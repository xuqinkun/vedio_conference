package util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;


public class InputChecker {

    public static boolean validInput(String input) {
        return !StringUtils.isEmpty(input);
    }

    public static boolean isValidEmail(String email) {
        Pattern compile = Pattern.compile("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?");
        return compile.matcher(email).matches();
    }

    public static void main(String[] args) {
        System.out.println(isValidEmail("111@.com"));
    }
}
