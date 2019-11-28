import { Component, OnInit } from '@angular/core';
import {MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-errorlog-details',
  templateUrl: './errorlog-details.component.html',
  styleUrls: ['./errorlog-details.component.css']
})
export class ErrorlogDetailsComponent implements OnInit {

  message;
  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  constructor(public dialogRef: MatDialogRef<ErrorlogDetailsComponent>) { }

  ngOnInit() {
  }

}
