package net.remgant.mbta;

/**
 * Created by jdr on 2/5/18.
 */
public class NoDataForTripException extends RuntimeException {
    public NoDataForTripException() {
    }

    public NoDataForTripException(String message) {
        super(message);
    }
}
