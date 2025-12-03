package SSL;

/**
 * Message types for the chat protocol.
 */
public enum MessageType {
    LOGIN_REQUEST,      // Client -> Server
    LOGIN_RESPONSE,     // Server -> Client
    JOIN_ROOM_REQUEST,
    TEXT_MESSAGE,
    PRIVATE_MESSAGE,
    USER_LIST_REQUEST,
    ERROR_RESPONSE, USER_LIST_RESPONSE
}
