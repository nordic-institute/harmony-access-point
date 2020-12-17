import { Component, OnInit } from '@angular/core';
import {MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-errorlog-details',
  templateUrl: './errorlog-details.component.html',
  styleUrls: ['./errorlog-details.component.css']
})
export class ErrorlogDetailsComponent {

  message;

  constructor(public dialogRef: MatDialogRef<ErrorlogDetailsComponent>) { }

}
