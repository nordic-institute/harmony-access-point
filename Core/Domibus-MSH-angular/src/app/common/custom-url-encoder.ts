import {HttpParameterCodec} from '@angular/common/http'

/**
 * Custom encoder to avoid angular issue encoding the '+' character
 *
 * @author Ion Perpegel
 * @since 4.2
 */
export class CustomURLEncoder implements HttpParameterCodec {
  encodeKey(key: string): string {
    return encodeURIComponent(key);
  }

  encodeValue(key: string): string {
    return encodeURIComponent(key);
  }

  decodeKey(key: string): string {
    return decodeURIComponent(key);
  }

  decodeValue(key: string) {
    return decodeURIComponent(key);
  }
}
