import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {RoutingCriteriaEntry} from '../support/routingcriteriaentry';
import {BackendFilterEntry} from '../support/backendfilterentry';
import {NgControl, NgForm} from '@angular/forms';

let NEW_MODE = 'New Message Filter';
let EDIT_MODE = 'Message Filter Edit';
let MAX_LENGTH = 255;

@Component({
  selector: 'editmessagefilter-form',
  templateUrl: 'editmessagefilter-form.component.html',
  styleUrls: ['editmessagefilter-form.component.css'],
})
export class EditMessageFilterComponent {

  formTitle: string;
  textMaxLength = MAX_LENGTH;

  backendFilterNames: Array<String> = [];

  entity: BackendFilterEntry;
  criteria: any;

  constructor(public dialogRef: MatDialogRef<EditMessageFilterComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.backendFilterNames = data.backendFilterNames;

    this.entity = this.data.entity;
    this.extractCriteria();

    this.formTitle = this.entity.persisted ? EDIT_MODE : NEW_MODE;
  }

  private extractCriteria() {
    this.criteria = BackendFilterEntry.routingCriteriaNames.reduce((map, key) => {
      map[key] = this.entity[key] ? this.entity[key].expression : null;
      return map;
    }, {});
  }

  submitForm(messageFilterForm: NgForm) {
    if (messageFilterForm.invalid) {
      return;
    }

    this.updateRoutingCriteria();
    this.entity.routingCriterias = BackendFilterEntry.routingCriteriaNames.map(name => this.entity[name]).filter(el => el != null);

    this.dialogRef.close(true);
  }

  private updateRoutingCriteria() {
    BackendFilterEntry.routingCriteriaNames.forEach(name => {
      if (this.criteria[name]) {
        if (this.entity[name]) { //update
          this.entity[name].expression = this.criteria[name];
        } else { //add
          this.entity[name] = new RoutingCriteriaEntry(null, name, this.criteria[name]);
        }
      } else { //delete if exists
        delete this.entity[name];
      }
    });
  }

  shouldShowErrors(field: NgControl): boolean {
    return (field.touched || field.dirty) && !!field.errors;
  }

  isFormDisabled(messageFilterForm: NgForm) {
    return messageFilterForm.invalid || (!messageFilterForm.dirty && this.entity.persisted);
  }
}
