// ChatGPT and Blackbox AI for avoiding blocking Event Dispatch Thread (EDT) during authentication
// EDT responsible for all GUI-related events and was previously blocked by while loop for authentication
// Callback interface used to notify other class when authentication is successful

public interface AuthenticationCallback {
    void onAuthenticationSuccess();
}
