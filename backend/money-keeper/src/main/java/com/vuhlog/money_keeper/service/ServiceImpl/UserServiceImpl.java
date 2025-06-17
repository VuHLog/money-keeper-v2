package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.dao.RoleRepository;
import com.vuhlog.money_keeper.dao.UserRoleRepository;
import com.vuhlog.money_keeper.dao.UsersRepository;
import com.vuhlog.money_keeper.dto.request.ChangePasswordRequest;
import com.vuhlog.money_keeper.dto.request.UserCreationRequest;
import com.vuhlog.money_keeper.dto.request.UserUpdateRequest;
import com.vuhlog.money_keeper.dto.response.UserResponse;
import com.vuhlog.money_keeper.entity.Role;
import com.vuhlog.money_keeper.entity.UserRole;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.UserMapper;
import com.vuhlog.money_keeper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getById(String id) {
        return userMapper.toUserResponse(usersRepository.findById(id).get());
    }

    @Override
    public UserResponse getByUsername(String username) {
        return userMapper.toUserResponse(usersRepository.findByUsername(username).get());
    }

    @Override
    public String changePassword(ChangePasswordRequest request) {
        Users user = getMyUserInfo();
        if(!passwordEncoder.matches(request.getCurrPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);
        return "Password changed successfully";
    }

    @Override
    public String changePasswordForgotPassword(ChangePasswordRequest request, String email) {
        Users user = usersRepository.findByEmailAndOAuth2(email, false);
        if(user == null) {
            throw new AppException(ErrorCode.INVALID_EMAIL);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);
        return "Password changed successfully";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse addUser(UserCreationRequest request) throws IOException {
        if(usersRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        if(usersRepository.findByEmailAndOAuth2(request.getEmail(), false) != null)
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        Users user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if(user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl("https://res.cloudinary.com/cloud1412/image/upload/v1739899158/hffbxsj6wbkbzkxjfetz.png");
        }

        //xu ly roles request
        Set<UserRole> user_roles = new HashSet<>();
        if(request.getRoles().isEmpty()) {
            UserRole user_role = new UserRole();
            user_role.setRole(roleRepository.findByRoleName("USER"));
            user_role.setUser(user);
            user_roles.add(user_role);
        }else {
            Users finalUser = user;
            request.getRoles().stream().forEach(s -> user_roles.add(new UserRole(finalUser,s)));
        }
        user.setUser_roles(user_roles);
        user = usersRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @Override
    public void executeSqlScriptForUser(String userId) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sql/insert_category.sql");
        if (inputStream == null) {
            throw new FileNotFoundException("SQL file not found in resources");
        }

        String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        sql = sql.replace(":userId", userId);

        String[] sqlStatements = sql.split(";");
        for (String statement : sqlStatements) {
            jdbcTemplate.execute(statement.trim());
        }
    }

    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return usersRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    @Override
    public Page<UserResponse> getUsersContains(String s, Pageable pageable) {
        return usersRepository.findByUsernameContainsIgnoreCase(s, pageable).map(userMapper::toUserResponse);
    }

    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Users user = usersRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }
    public Users getMyUserInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return usersRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Override
    public Users getUserByEmail(String email, Boolean oauth2) {
        return usersRepository.findByEmailAndOAuth2(email, oauth2);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        Users user = usersRepository.findById(userId).get();
        if(!user.getEmail().equals(request.getEmail()) && usersRepository.findByEmailAndOAuth2(request.getEmail(), false) != null) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(usersRepository.saveAndFlush(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String userId) {
        userRoleRepository.deleteByUserId(userId);
        usersRepository.deleteById(userId);
    }
}
