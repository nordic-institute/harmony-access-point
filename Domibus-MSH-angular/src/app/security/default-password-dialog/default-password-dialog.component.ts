import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-default-password-dialog',
  templateUrl: './default-password-dialog.component.html',
  styleUrls: ['./default-password-dialog.component.css']
})
export class DefaultPasswordDialogComponent implements OnInit {

  reason: string;

  constructor (public dialogRef: MatDialogRef<DefaultPasswordDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.reason = data;
  }

  ngOnInit () {
  }

}
