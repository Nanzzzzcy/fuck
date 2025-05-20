package eventManageSystem;

public class UnauthorizedAccessException extends RuntimeException {
	public UnauthorizedAccessException(String message) {
		super(message);
	}
}