import { Component, OnInit, Inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-party-identifier-details',
  templateUrl: './party-identifier-details.component.html',
  styleUrls: ['./party-identifier-details.component.css']
})
export class PartyIdentifierDetailsComponent implements OnInit {
  public partyIdTypePattern = 'urn:oasis:names:tc:ebcore:partyid\\-type:[a-zA-Z0-9_:-]+';
  public partyIdTypeMessage = 'You should follow the rule: urn:oasis:names:tc:ebcore:partyid-type:[....]';

  partyIdentifier: any;

  constructor(public dialogRef: MatDialogRef<PartyIdentifierDetailsComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.partyIdentifier = data.edit;
  }

  ngOnInit() {
  }

  ok() {
    this.dialogRef.close(true);
  }

  cancel() {
    this.dialogRef.close(false);
  }
}
