package com.booking;
import org.mindrot.jbcrypt.BCrypt;
public class HashMaker {
    public static void main(String[] args) {
        System.out.println("HASH_IS:" + BCrypt.hashpw("YOUR_DESIRED_PASSWORD_HERE", BCrypt.gensalt(10)));
    }
}
