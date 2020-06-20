/* Custom Exceptions class to store custom exceptions used to pass error types using asynchronous callbacks
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

public class CustomExceptions {
    public static class NameTakenException extends Exception { //Exception for nickname taken
        public NameTakenException(String message) {
            super(message);
        }
    }

    public static class BadNameException extends Exception { //Exception for an invalid or not allowed nickname/username
        public BadNameException(String message) {
            super(message);
        }
    }

    public static class ChannelJoinPendingException extends Exception { //Exception for when a join request is already pending when a new one is created
        public ChannelJoinPendingException(String message) {
            super(message);
        }
    }

    public static class IllegalChannelNameException extends Exception { //Exception for a channel name that doesn't fit the IRC server's channel naming scheme
        public IllegalChannelNameException(String message) {
            super(message);
        }
    }

    public static class ChannelFullException extends Exception { //Exception for when trying to join a full channel
        public ChannelFullException(String message) {
            super(message);
        }
    }
}
