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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

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
    public UserResponse addUser(UserCreationRequest request) {
        if(usersRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);


        Users user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //xu ly roles request
        Set<UserRole> user_roles = new HashSet<>();
        if(request.getRoles().isEmpty()) {
            UserRole user_role = new UserRole();
            user_role.setRole(roleRepository.findByRoleName("USER"));
            user_role.setUser(user);
            user_roles.add(user_role);
        }else {
            request.getRoles().stream().forEach(s -> user_roles.add(new UserRole(user,s)));
        }
        user.setUser_roles(user_roles);

        return userMapper.toUserResponse(usersRepository.save(user));
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
    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        Users user = usersRepository.findById(userId).get();
        userMapper.updateUser(user, request);

        // xoá user id trong user_role
        userRoleRepository.deleteByUser(user);

        Set<UserRole> user_roles = new HashSet<>();
        List<Role> rolesRequest = request.getRoles().stream().toList();
        for(int i=0; i<rolesRequest.size();i++){
            user_roles.add(new UserRole(user,rolesRequest.get(i)));
        }

        user.setUser_roles(user_roles);

        return userMapper.toUserResponse(usersRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        userRoleRepository.deleteByUserId(userId);
        usersRepository.deleteById(userId);
    }
}
