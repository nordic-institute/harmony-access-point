import {Component, Inject} from "@angular/core";
import {TrustStoreEntry} from "../support/trustore.model";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

/**
 * @Author Dussart Thomas
 * @Since 3.3
 */

@Component({
  selector: 'app-truststore-dialog',
  templateUrl: './truststore-dialog.component.html',
  styleUrls: ['./truststore-dialog.component.css']
})
export class TruststoreDialogComponent {

  trustStoreEntry: TrustStoreEntry;

  constructor(public dialogRef: MatDialogRef<TruststoreDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.trustStoreEntry = data.trustStoreEntry;
  }

}
