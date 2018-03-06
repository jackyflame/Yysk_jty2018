package im.socks.yysk.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cole on 2017/11/1.
 * <p>
 * 这里的代码来自commons-validator ，但是如果直接使用这个库，需要添加太多的依赖，这里就仅仅使用部分代码
 */

public class IPValidator {

    private static final int IPV4_MAX_OCTET_VALUE = 255;

    private static final int MAX_UNSIGNED_SHORT = 0xffff;

    private static final int BASE_16 = 16;


    private static final String IPV4_REGEX =
            "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";

    // Max number of hex groups (separated by :) in an IPV6 address
    private static final int IPV6_MAX_HEX_GROUPS = 8;

    // Max hex digits in each IPv6 group
    private static final int IPV6_MAX_HEX_DIGITS_PER_GROUP = 4;

    /**
     * Singleton instance of this class.
     */
    private static final IPValidator VALIDATOR = new IPValidator();

    /**
     * IPv4 RegexValidator
     */
    private final RegexValidator ipv4Validator = new RegexValidator(IPV4_REGEX);

    /**
     * Returns the singleton instance of this validator.
     *
     * @return the singleton instance of this validator
     */
    public static IPValidator getInstance() {
        return VALIDATOR;
    }

    /**
     * Checks if the specified string is a valid IP address.
     *
     * @param inetAddress the string to validate
     * @return true if the string validates as an IP address
     */
    public boolean isValid(String inetAddress) {
        return isValidInet4Address(inetAddress) || isValidInet6Address(inetAddress);
    }

    /**
     * Validates an IPv4 address. Returns true if valid.
     *
     * @param inet4Address the IPv4 address to validate
     * @return true if the argument contains a valid IPv4 address
     */
    public boolean isValidInet4Address(String inet4Address) {
        // verify that address conforms to generic IPv4 format
        String[] groups = ipv4Validator.match(inet4Address);

        if (groups == null) {
            return false;
        }

        // verify that address subgroups are legal
        for (String ipSegment : groups) {
            if (ipSegment == null || ipSegment.length() == 0) {
                return false;
            }

            int iIpSegment = 0;

            try {
                iIpSegment = Integer.parseInt(ipSegment);
            } catch (NumberFormatException e) {
                return false;
            }

            if (iIpSegment > IPV4_MAX_OCTET_VALUE) {
                return false;
            }

            if (ipSegment.length() > 1 && ipSegment.startsWith("0")) {
                return false;
            }

        }

        return true;
    }

    /**
     * Validates an IPv6 address. Returns true if valid.
     *
     * @param inet6Address the IPv6 address to validate
     * @return true if the argument contains a valid IPv6 address
     * @since 1.4.1
     */
    public boolean isValidInet6Address(String inet6Address) {
        boolean containsCompressedZeroes = inet6Address.contains("::");
        if (containsCompressedZeroes && (inet6Address.indexOf("::") != inet6Address.lastIndexOf("::"))) {
            return false;
        }
        if ((inet6Address.startsWith(":") && !inet6Address.startsWith("::"))
                || (inet6Address.endsWith(":") && !inet6Address.endsWith("::"))) {
            return false;
        }
        String[] octets = inet6Address.split(":");
        if (containsCompressedZeroes) {
            List<String> octetList = new ArrayList<String>(Arrays.asList(octets));
            if (inet6Address.endsWith("::")) {
                // String.split() drops ending empty segments
                octetList.add("");
            } else if (inet6Address.startsWith("::") && !octetList.isEmpty()) {
                octetList.remove(0);
            }
            octets = octetList.toArray(new String[octetList.size()]);
        }
        if (octets.length > IPV6_MAX_HEX_GROUPS) {
            return false;
        }
        int validOctets = 0;
        int emptyOctets = 0; // consecutive empty chunks
        for (int index = 0; index < octets.length; index++) {
            String octet = octets[index];
            if (octet.length() == 0) {
                emptyOctets++;
                if (emptyOctets > 1) {
                    return false;
                }
            } else {
                emptyOctets = 0;
                // Is last chunk an IPv4 address?
                if (index == octets.length - 1 && octet.contains(".")) {
                    if (!isValidInet4Address(octet)) {
                        return false;
                    }
                    validOctets += 2;
                    continue;
                }
                if (octet.length() > IPV6_MAX_HEX_DIGITS_PER_GROUP) {
                    return false;
                }
                int octetInt = 0;
                try {
                    octetInt = Integer.parseInt(octet, BASE_16);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (octetInt < 0 || octetInt > MAX_UNSIGNED_SHORT) {
                    return false;
                }
            }
            validOctets++;
        }
        if (validOctets > IPV6_MAX_HEX_GROUPS || (validOctets < IPV6_MAX_HEX_GROUPS && !containsCompressedZeroes)) {
            return false;
        }
        return true;
    }

    public static class RegexValidator implements Serializable {

        private static final long serialVersionUID = -8832409930574867162L;

        private final Pattern[] patterns;

        /**
         * Construct a <i>case sensitive</i> validator for a single
         * regular expression.
         *
         * @param regex The regular expression this validator will
         *              validate against
         */
        public RegexValidator(String regex) {
            this(regex, true);
        }

        /**
         * Construct a validator for a single regular expression
         * with the specified case sensitivity.
         *
         * @param regex         The regular expression this validator will
         *                      validate against
         * @param caseSensitive when <code>true</code> matching is <i>case
         *                      sensitive</i>, otherwise matching is <i>case in-sensitive</i>
         */
        public RegexValidator(String regex, boolean caseSensitive) {
            this(new String[]{regex}, caseSensitive);
        }

        /**
         * Construct a <i>case sensitive</i> validator that matches any one
         * of the set of regular expressions.
         *
         * @param regexs The set of regular expressions this validator will
         *               validate against
         */
        public RegexValidator(String[] regexs) {
            this(regexs, true);
        }

        /**
         * Construct a validator that matches any one of the set of regular
         * expressions with the specified case sensitivity.
         *
         * @param regexs        The set of regular expressions this validator will
         *                      validate against
         * @param caseSensitive when <code>true</code> matching is <i>case
         *                      sensitive</i>, otherwise matching is <i>case in-sensitive</i>
         */
        public RegexValidator(String[] regexs, boolean caseSensitive) {
            if (regexs == null || regexs.length == 0) {
                throw new IllegalArgumentException("Regular expressions are missing");
            }
            patterns = new Pattern[regexs.length];
            int flags = (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            for (int i = 0; i < regexs.length; i++) {
                if (regexs[i] == null || regexs[i].length() == 0) {
                    throw new IllegalArgumentException("Regular expression[" + i + "] is missing");
                }
                patterns[i] = Pattern.compile(regexs[i], flags);
            }
        }

        /**
         * Validate a value against the set of regular expressions.
         *
         * @param value The value to validate.
         * @return <code>true</code> if the value is valid
         * otherwise <code>false</code>.
         */
        public boolean isValid(String value) {
            if (value == null) {
                return false;
            }
            for (int i = 0; i < patterns.length; i++) {
                if (patterns[i].matcher(value).matches()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Validate a value against the set of regular expressions
         * returning the array of matched groups.
         *
         * @param value The value to validate.
         * @return String array of the <i>groups</i> matched if
         * valid or <code>null</code> if invalid
         */
        public String[] match(String value) {
            if (value == null) {
                return null;
            }
            for (int i = 0; i < patterns.length; i++) {
                Matcher matcher = patterns[i].matcher(value);
                if (matcher.matches()) {
                    int count = matcher.groupCount();
                    String[] groups = new String[count];
                    for (int j = 0; j < count; j++) {
                        groups[j] = matcher.group(j + 1);
                    }
                    return groups;
                }
            }
            return null;
        }


        /**
         * Validate a value against the set of regular expressions
         * returning a String value of the aggregated groups.
         *
         * @param value The value to validate.
         * @return Aggregated String value comprised of the
         * <i>groups</i> matched if valid or <code>null</code> if invalid
         */
        public String validate(String value) {
            if (value == null) {
                return null;
            }
            for (int i = 0; i < patterns.length; i++) {
                Matcher matcher = patterns[i].matcher(value);
                if (matcher.matches()) {
                    int count = matcher.groupCount();
                    if (count == 1) {
                        return matcher.group(1);
                    }
                    StringBuilder buffer = new StringBuilder();
                    for (int j = 0; j < count; j++) {
                        String component = matcher.group(j + 1);
                        if (component != null) {
                            buffer.append(component);
                        }
                    }
                    return buffer.toString();
                }
            }
            return null;
        }

        /**
         * Provide a String representation of this validator.
         *
         * @return A String representation of this validator
         */
        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("RegexValidator{");
            for (int i = 0; i < patterns.length; i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(patterns[i].pattern());
            }
            buffer.append("}");
            return buffer.toString();
        }

    }
}
