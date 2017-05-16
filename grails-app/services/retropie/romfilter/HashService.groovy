package retropie.romfilter

import javax.xml.bind.DatatypeConverter
import java.security.MessageDigest

class HashService {
    /**
     * MD5 hash a String.
     *
     * @param data
     * @return
     */
    String hash(String data) {
        return DatatypeConverter.printHexBinary(
            MessageDigest.getInstance("MD5").digest(data.getBytes("UTF-8")));
    }
}