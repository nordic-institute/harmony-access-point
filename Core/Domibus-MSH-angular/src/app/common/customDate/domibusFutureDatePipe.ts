import {Pipe, PipeTransform} from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';

@Pipe({
  name: 'domibusFutureDate'
})
export class DomibusFutureDatePipe implements PipeTransform {

  constructor(private datePipe: DatePipe, private decimalPipe: DecimalPipe) {
  }

  transform(reprogrammable: { nextAttempt: Date, nextAttemptTimezoneId: string, nextAttemptOffsetSeconds: number }): string {
    if (reprogrammable && reprogrammable.nextAttempt) {
      if (reprogrammable.nextAttemptTimezoneId) {
        const offsetHours = reprogrammable.nextAttemptOffsetSeconds / 3600;
        const timezoneFormattedDate = this.datePipe.transform(reprogrammable.nextAttempt, 'dd-MM-yyyy HH:mm:ss', this.getTimezoneOffset(offsetHours));
        const utcOffset = this.getUtcOffset(offsetHours);
        return `${timezoneFormattedDate}${utcOffset}`;
      } else {
        const timezoneAgnosticFormattedDate = this.datePipe.transform(reprogrammable.nextAttempt, 'dd-MM-yyyy HH:mm:ss');
        return `${timezoneAgnosticFormattedDate}`;
      }
    }
  }

  private getTimezoneOffset(offsetHours: number): string {
    const formattedOffsetHours = this.decimalPipe.transform(offsetHours, '2.2-2');
    const formattedTimezoneOffset = `${offsetHours >= 0 ? '+' : ''}${formattedOffsetHours}`;
    return formattedTimezoneOffset;
  }

  private getUtcOffset(offsetHours: number) {
    const formattedOffsetHours = this.decimalPipe.transform(offsetHours, '1.0-1');
    let formattedUtcOffset = 'UTC';
    if (offsetHours <= -0.5 || offsetHours >= 0.5) {
      formattedUtcOffset = `${formattedUtcOffset}${offsetHours > 0 ? '+' : ''}${formattedOffsetHours}`;
    }
    return formattedUtcOffset;
  }
}
