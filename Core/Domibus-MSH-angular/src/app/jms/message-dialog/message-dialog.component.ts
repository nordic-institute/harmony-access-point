import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-message-dialog',
  templateUrl: './message-dialog.component.html',
  styleUrls: ['./message-dialog.component.css']
})
export class MessageDialogComponent {

  message: any;
  currentSearchSelectedSource: any;

  constructor(public dialogRef: MatDialogRef<MessageDialogComponent>, private changeDetector: ChangeDetectorRef) {
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

}
