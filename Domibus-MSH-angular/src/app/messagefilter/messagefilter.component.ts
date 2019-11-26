import {Component, OnInit} from '@angular/core';
import {MdDialog, MdDialogRef} from '@angular/material';
import {AlertService} from '../common/alert/alert.service';
import {Http, Headers, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {MessageFilterResult} from './messagefilterresult';
import {BackendFilterEntry} from './backendfilterentry';
import {RoutingCriteriaEntry} from './routingcriteriaentry';
import {isNullOrUndefined} from 'util';
import {EditMessageFilterComponent} from './editmessagefilter-form/editmessagefilter-form.component';
import {DirtyOperations} from '../common/dirty-operations';
import {CancelDialogComponent} from '../common/cancel-dialog/cancel-dialog.component';
import {SaveDialogComponent} from '../common/save-dialog/save-dialog.component';
import {DownloadService} from '../common/download.service';

@Component({
  moduleId: module.id,
  templateUrl: 'messagefilter.component.html',
  providers: [],
  styleUrls: ['./messagefilter.component.css']
})

export class MessageFilterComponent implements OnInit, DirtyOperations {
  static readonly MESSAGE_FILTER_URL: string = 'rest/messagefilters';

  rows: any [];
  selected: any[];

  backendFilterNames: any[];

  rowNumber: number;

  enableCancel: boolean;
  enableSave: boolean;
  enableDelete: boolean;
  enableEdit: boolean;
  enableMoveUp: boolean;
  enableMoveDown: boolean;

  loading: boolean;
  areFiltersPersisted: boolean;
  dirty: boolean;
  routingCriterias = ['from', 'to', 'action', 'service'];

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  ngOnInit() {
    this.rows = [];
    this.selected = [];

    this.backendFilterNames = [];

    this.rowNumber = -1;

    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;
    this.enableEdit = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    this.loading = true;

    this.getBackendFiltersInfo();
  }

  getBackendFiltersInfo() {
    this.dirty = false;
    this.getMessageFilterEntries().subscribe((result: MessageFilterResult) => {

      let newRows = [];
      this.backendFilterNames = [];
      if (!isNullOrUndefined(result.messageFilterEntries)) {
        for (let i = 0; i < result.messageFilterEntries.length; i++) {
          let currentFilter: BackendFilterEntry = result.messageFilterEntries[i];
          if (isNullOrUndefined(currentFilter)) {
            continue;
          }
          let backendEntry = new BackendFilterEntry(currentFilter.entityId, i, currentFilter.backendName, currentFilter.routingCriterias, currentFilter.persisted);
          newRows.push(backendEntry);
          if (this.backendFilterNames.indexOf(backendEntry.backendName) == -1) {
            this.backendFilterNames.push(backendEntry.backendName);
          }
        }
        this.areFiltersPersisted = result.areFiltersPersisted;

        this.rows = newRows;

        if (!this.areFiltersPersisted && this.backendFilterNames.length > 1) {
          this.alertService.error('One or several filters in the table were not configured yet (Persisted flag is not checked). ' +
            'It is strongly recommended to double check the filters configuration and afterwards save it.');
          this.enableSave = true;
        }
      }
    }, (error: any) => {
      console.log('error getting the message filter: ' + error);
      this.loading = false;
      this.alertService.exception('Error occurred: ', error);
    });
  }

  getMessageFilterEntries(): Observable<MessageFilterResult> {
    return this.http.get(MessageFilterComponent.MESSAGE_FILTER_URL).map((response: Response) =>
      response.json()
    );
  }

  createValueProperty(prop, newPropValue, row) {
    this.rows[row][prop] = newPropValue;
  }

  buttonNew() {
    let formRef: MdDialogRef<EditMessageFilterComponent> = this.dialog.open(EditMessageFilterComponent, {data: {backendFilterNames: this.backendFilterNames}});
    formRef.afterClosed().subscribe(result => {
      if (result == true) {
        let backendEntry = this.createEntry(formRef);
        if (this.findRowsIndex(backendEntry) == -1) {
          this.rows.push(backendEntry);

          this.setDirty(formRef.componentInstance.messageFilterForm.dirty);
        } else {
          this.alertService.error('Impossible to insert a duplicate entry');
        }
      }
    });
  }

  private findRowsIndex(backendEntry: BackendFilterEntry): number {
    for (let i = 0; i < this.rows.length; i++) {
      let currentRow = this.rows[i];
      if (currentRow.backendName === backendEntry.backendName
        && this.compareRoutingCriterias(backendEntry.routingCriterias, currentRow.routingCriterias)) {
        return i;
      }
    }
    return -1;
  }

  private compareRoutingCriterias(criteriasA: RoutingCriteriaEntry[], criteriasB: RoutingCriteriaEntry[]): boolean {
    let found: boolean = true;
    for (let entry of criteriasA) {
      found = found && this.findRoutingCriteria(entry, criteriasB);
    }
    for (let entry of criteriasB) {
      found = found && this.findRoutingCriteria(entry, criteriasA);
    }
    return found;
  }

  private findRoutingCriteria(toFind: RoutingCriteriaEntry, routingCriterias: RoutingCriteriaEntry[]): boolean {
    for (let entry of routingCriterias) {
      if (entry.name === toFind.name && entry.expression === toFind.expression) {
        return true;
      }
    }
    return toFind.expression === '' && routingCriterias.length == 0;
  }

  buttonEditAction(row) {
    let formRef: MdDialogRef<EditMessageFilterComponent> = this.dialog.open(EditMessageFilterComponent, {
      data: {
        backendFilterNames: this.backendFilterNames,
        edit: row
      }
    });
    formRef.afterClosed().subscribe(result => {
      if (result == true) {
        let backendEntry = this.createEntry(formRef);
        let backendEntryPos = this.findRowsIndex(backendEntry);
        if (backendEntryPos == -1) {
          this.updateSelectedPlugin(formRef.componentInstance.plugin);

          for (var criteria of this.routingCriterias) {
            this.updateSelectedProperty(criteria, formRef.componentInstance[criteria]);
          }

          this.setDirty(formRef.componentInstance.messageFilterForm.dirty);
        } else {
          if (backendEntryPos != this.rowNumber) {
            this.alertService.error('Impossible to insert a duplicate entry');
          }
        }
      }
    });
  }

  private createEntry(formRef: MdDialogRef<EditMessageFilterComponent>) {
    let routingCriterias: Array<RoutingCriteriaEntry> = [];

    for (var criteria of this.routingCriterias) {
      if (!!formRef.componentInstance[criteria]) {
        routingCriterias.push(new RoutingCriteriaEntry(0, criteria, formRef.componentInstance[criteria]));
      }
    }

    let backendEntry = new BackendFilterEntry(0, this.rowNumber + 1, formRef.componentInstance.plugin, routingCriterias, false);
    return backendEntry;
  }

  private deleteRoutingCriteria(rc: string) {
    let numRoutingCriterias = this.rows[this.rowNumber].routingCriterias.length;
    for (let i = 0; i < numRoutingCriterias; i++) {
      let routCriteria = this.rows[this.rowNumber].routingCriterias[i];
      if (routCriteria.name == rc) {
        this.rows[this.rowNumber].routingCriterias.splice(i, 1);
        return;
      }
    }
  }

  private createRoutingCriteria(rc: string, value: string) {
    if (value.length == 0) {
      return;
    }
    let newRC = new RoutingCriteriaEntry(null, rc, value);
    this.rows[this.rowNumber].routingCriterias.push(newRC);
    this.createValueProperty(rc, newRC, this.rowNumber);
  }

  private updateSelectedPlugin(value: string) {
    this.rows[this.rowNumber].backendName = value;
  }

  private updateSelectedProperty(prop: string, value: string) {
    if (!isNullOrUndefined(this.rows[this.rowNumber][prop])) {
      if (value.length == 0) {
        // delete
        this.deleteRoutingCriteria(prop);
        this.rows[this.rowNumber][prop].expression = '';
      } else {
        // update
        this.rows[this.rowNumber][prop].expression = value;
      }
    } else {
      // create
      this.createRoutingCriteria(prop, value);
    }
  }

  private disableSelectionAndButtons() {
    this.selected = [];
    this.enableMoveDown = false;
    this.enableMoveUp = false;
    this.enableCancel = false;
    this.enableSave = false;
    this.enableEdit = false;
    this.enableDelete = false;
  }

  saveAsCSV() {
    if (this.isDirty()) {
      this.saveDialog(true);
    } else {
      DownloadService.downloadNative(MessageFilterComponent.MESSAGE_FILTER_URL + '/csv');
    }
  }

  cancelDialog() {
    let dialogRef = this.dialog.open(CancelDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.disableSelectionAndButtons();
        this.getBackendFiltersInfo();
      }
    });
  }

  saveDialog(withDownloadCSV: boolean) {
    this.dialog.open(SaveDialogComponent).afterClosed().subscribe(result => {
      if (result) {
        this.disableSelectionAndButtons();
        this.http.put(MessageFilterComponent.MESSAGE_FILTER_URL, this.rows).subscribe(res => {
          this.alertService.success('The operation \'update message filters\' completed successfully.', false);
          this.getBackendFiltersInfo();
          if (withDownloadCSV) {
            DownloadService.downloadNative(MessageFilterComponent.MESSAGE_FILTER_URL + '/csv');
          }
        }, err => {
          this.alertService.exception('The operation \'update message filters\' not completed successfully.', err);
        });
      } else {
        if (withDownloadCSV) {
          DownloadService.downloadNative(MessageFilterComponent.MESSAGE_FILTER_URL + '/csv');
        }
      }
    });
  }

  buttonDeleteAction(row) {
    this.deleteItems([row]);
  }

  buttonDelete() {
    this.deleteItems(this.selected);
  }

  private deleteItems(items: any[]) {
    this.setDirty(true);

    this.enableDelete = false;
    this.enableEdit = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    // we need to use the old for loop approach to don't mess with the entries on the top before
    for (let i = items.length - 1; i >= 0; i--) {
      this.rows.splice(items[i].$$index, 1);
    }

    this.selected = [];
  }

  private moveUpInternal(rowNumber) {
    if (rowNumber < 1) {
      return;
    }
    let array = this.rows.slice();
    let move = array[rowNumber];
    array[rowNumber] = array[rowNumber - 1];
    array[rowNumber - 1] = move;

    this.rows = array.slice();
    this.rowNumber--;

    if (rowNumber == 0) {
      this.enableMoveUp = false;
    }
    this.enableMoveDown = true;

    this.setDirty(true);
  }

  buttonMoveUpAction(row) {
    this.moveUpInternal(row.$$index);
    setTimeout(() => {
      document.getElementById('pluginRow' + (row.$$index) + '_id').click();
    }, 50);
  }

  buttonMoveUp() {
    this.buttonMoveUpAction(this.selected[0]);
  }

  private moveDownInternal(rowNumber) {
    if (rowNumber > this.rows.length - 1) {
      return;
    }

    let array = this.rows.slice();
    let move = array[rowNumber];
    array[rowNumber] = array[rowNumber + 1];
    array[rowNumber + 1] = move;

    this.rows = array.slice();
    this.rowNumber++;

    if (rowNumber == this.rows.length - 1) {
      this.enableMoveDown = false;
    }
    this.enableMoveUp = true;

    this.setDirty(true);
  }

  buttonMoveDownAction(row) {
    this.moveDownInternal(row.$$index);
    setTimeout(() => {
      document.getElementById('pluginRow' + (row.$$index) + '_id').click();
    }, 50);
  }

  buttonMoveDown() {
    this.buttonMoveDownAction(this.selected[0]);
  }

  onSelect({selected}) {
    if (isNullOrUndefined(selected) || selected.length == 0) {
      // unselect
      this.enableMoveDown = false;
      this.enableMoveUp = false;
      this.enableDelete = false;
      this.enableEdit = false;

      return;
    }

    // select
    this.rowNumber = this.selected[0].$$index;

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableMoveDown = selected.length == 1 && this.rowNumber < this.rows.length - 1;
    this.enableMoveUp = selected.length == 1 && this.rowNumber > 0;
    this.enableDelete = selected.length > 0;
    this.enableEdit = selected.length == 1;
  }

  isDirty(): boolean {
    return this.enableCancel;
  }

  setDirty(itemValue: boolean) {
    this.dirty = this.dirty || itemValue;
    this.enableSave = this.dirty;
    this.enableCancel = this.dirty;
  }

  onActivate(event) {
    if ('dblclick' === event.type) {
      this.buttonEditAction(event.row);
    }
  }
}
