import {Component, OnInit, Inject} from '@angular/core';
import {MD_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-messagelog-details',
  templateUrl: './messagelog-details.component.html',
  styleUrls: ['./messagelog-details.component.css']
})
export class MessagelogDetailsComponent implements OnInit {

  message;
  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';
  fourCornerEnabled;

  constructor(public dialogRef: MatDialogRef<MessagelogDetailsComponent>, @Inject(MD_DIALOG_DATA) public data: any) {
    this.message = data.message;
    this.fourCornerEnabled = data.fourCornerEnabled;
  }

  ngOnInit() {
  }

}
