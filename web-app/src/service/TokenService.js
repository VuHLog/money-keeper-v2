class TokenService{
    getSessionAccessToken(){
        const token = sessionStorage.getItem("token");
        return token;
    }

    setSessionAccessToken(token){
        sessionStorage.setItem("token", token);
    }

    removeSessionAccessToken(){
        sessionStorage.removeItem("token");
    }
}
export default new TokenService();