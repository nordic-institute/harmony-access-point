import {Component, OnInit, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-messagelog-details',
  templateUrl: './messagelog-details.component.html',
  styleUrls: ['./messagelog-details.component.css']
})
export class MessagelogDetailsComponent {

  message;
  fourCornerEnabled;

  constructor(public dialogRef: MatDialogRef<MessagelogDetailsComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.message = data.message;
    this.fourCornerEnabled = data.fourCornerEnabled;
  }

}
