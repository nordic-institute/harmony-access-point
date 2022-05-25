import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';

/**
 * Session expiration dialog component.
 *
 * @since 4.2
 * @author Sebastian-Ion TINCU
 */
@Component({
  selector: 'app-session-expired-dialog',
  templateUrl: './session-expired-dialog.component.html'
})
export class SessionExpiredDialogComponent {

  reason: string;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
    this.reason = data;
  }

}
