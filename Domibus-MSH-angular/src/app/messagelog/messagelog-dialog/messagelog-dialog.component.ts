import {Component, OnInit} from '@angular/core';
import {MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-messagelog-dialog',
  templateUrl: './messagelog-dialog.component.html',
  styleUrls: ['./messagelog-dialog.component.css']
})
export class MessagelogDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<MessagelogDialogComponent>) {
  }

  ngOnInit() {
  }

}
