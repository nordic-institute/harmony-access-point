import {Component, OnInit} from '@angular/core';
import {MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-cancel-dialog',
  templateUrl: './cancel-dialog.component.html',
  styleUrls: ['./cancel-dialog.component.css']
})
export class CancelDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<CancelDialogComponent>) {
  }

  ngOnInit() {
  }

}
