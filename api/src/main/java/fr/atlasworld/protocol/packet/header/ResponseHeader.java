package fr.atlasworld.protocol.packet.header;

/**
 * Response specific header.
 */
public interface ResponseHeader extends Header {

    /**
     * Response code of the remote to the request.
     *
     * @return response code
     */
    short responseCode();

    /**
     * Checks whether the response returns a successful response.
     *
     * @return true if the response is successful, false otherwise.
     */
    boolean successResponse();
}
