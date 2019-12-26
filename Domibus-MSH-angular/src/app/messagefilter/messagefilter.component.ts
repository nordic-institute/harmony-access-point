import {Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material';
import {AlertService} from '../common/alert/alert.service';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {MessageFilterResult} from './messagefilterresult';
import {BackendFilterEntry} from './backendfilterentry';
import {RoutingCriteriaEntry} from './routingcriteriaentry';
import {EditMessageFilterComponent} from './editmessagefilter-form/editmessagefilter-form.component';
import {DirtyOperations} from '../common/dirty-operations';
import {DialogsService} from '../common/dialogs/dialogs.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import ModifiableListMixin from '../common/mixins/modifiable-list.mixin';

@Component({
  moduleId: module.id,
  templateUrl: 'messagefilter.component.html',
  providers: [],
  styleUrls: ['./messagefilter.component.css']
})

export class MessageFilterComponent extends mix(BaseListComponent)
  .with(ModifiableListMixin)
  implements OnInit, DirtyOperations {

  static readonly MESSAGE_FILTER_URL: string = 'rest/messagefilters';

  backendFilterNames: any[];

  rowNumber: number;

  enableCancel: boolean;
  enableSave: boolean;
  enableDelete: boolean;
  enableEdit: boolean;
  enableMoveUp: boolean;
  enableMoveDown: boolean;

  areFiltersPersisted: boolean;
  routingCriterias = ['from', 'to', 'action', 'service'];

  constructor(private http: HttpClient, private alertService: AlertService, public dialog: MatDialog, private dialogsService: DialogsService) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    this.backendFilterNames = [];

    this.rowNumber = -1;

    this.disableSelectionAndButtons();

    this.loadServerData();
  }

  public get name(): string {
    return 'Message Filters';
  }

  async getDataAndSetResults(): Promise<any> {
    this.getBackendFiltersInfo();
    this.disableSelectionAndButtons();
  }

  getBackendFiltersInfo() {
    return this.getMessageFilterEntries().toPromise().then((result: MessageFilterResult) => {
      let newRows = [];
      this.backendFilterNames = [];
      if (result.messageFilterEntries) {
        for (let i = 0; i < result.messageFilterEntries.length; i++) {
          let currentFilter: BackendFilterEntry = result.messageFilterEntries[i];
          if (!(currentFilter)) {
            continue;
          }
          let backendEntry = new BackendFilterEntry(currentFilter.entityId, i, currentFilter.backendName, currentFilter.routingCriterias, currentFilter.persisted);
          newRows.push(backendEntry);
          if (this.backendFilterNames.indexOf(backendEntry.backendName) == -1) {
            this.backendFilterNames.push(backendEntry.backendName);
          }
        }
        this.areFiltersPersisted = result.areFiltersPersisted;

        super.rows = newRows;
        super.count = newRows.length;

        if (!this.areFiltersPersisted && this.backendFilterNames.length > 1) {
          this.alertService.error('One or several filters in the table were not configured yet (Persisted flag is not checked). ' +
            'It is strongly recommended to double check the filters configuration and afterwards save it.');
          this.enableSave = true;
        }
      }
    });
  }

  getMessageFilterEntries(): Observable<MessageFilterResult> {
    return this.http.get<MessageFilterResult>(MessageFilterComponent.MESSAGE_FILTER_URL);
  }

  add() {
    let backendEntry = new BackendFilterEntry(0, this.rows.length + 1, this.backendFilterNames[0], [], false);
    this.dialog.open(EditMessageFilterComponent, {
      data: {
        backendFilterNames: this.backendFilterNames,
        entity: backendEntry
      }
    }).afterClosed().toPromise().then(ok => {
      if (ok) {
        // let backendEntry = this.createEntry(result);
        if (this.findRowIndexWithSameRoutingCriteria(backendEntry) == -1) {
          super.rows = [...this.rows, backendEntry];
          super.count = this.rows.length + 1;

          this.setDirty(true);

          // this.setDirty(result.messageFilterForm.dirty);
        } else {
          this.alertService.error('Impossible to insert a duplicate entry');
        }
      }
    });
  }

  edit(row?) {
    row = row || this.selected[0];

    const backendEntry = JSON.parse(JSON.stringify(row));
    this.dialog.open(EditMessageFilterComponent, {
      data: {
        backendFilterNames: this.backendFilterNames,
        entity: backendEntry
      }
    }).afterClosed().toPromise().then(ok => {
      if (ok) {
        let backendEntryPos = this.findRowIndexWithSameRoutingCriteria(backendEntry);
        if (backendEntryPos == -1) {
          this.rows.splice(this.rowNumber, 1, backendEntry);
          super.rows = [...this.rows];
          super.count = this.rows.length;

          this.setDirty(true);
        } else {
          if (backendEntryPos != this.rowNumber) {
            this.alertService.error('Impossible to insert a duplicate entry');
          }
        }

        setTimeout(() => {
          document.getElementById('pluginRow' + (this.rowNumber) + '_id').click();
        }, 50);

      }
    });
  }

  get csvUrl(): string {
    return MessageFilterComponent.MESSAGE_FILTER_URL + '/csv';
  }

  async doSave(): Promise<any> {
    this.disableSelectionAndButtons();
    return this.http.put(MessageFilterComponent.MESSAGE_FILTER_URL, this.rows).toPromise().then(res => {
      this.getBackendFiltersInfo();
    });
  }

  buttonDeleteAction(row) {
    this.deleteItems([row]);
  }

  delete() {
    this.deleteItems(this.selected);
  }

  private deleteItems(items: any[]) {
    this.setDirty(true);

    this.enableDelete = false;
    this.enableEdit = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    let copy = [...this.rows];
    // we need to use the old for loop approach to don't mess with the entries on the top before
    for (let i = items.length - 1; i >= 0; i--) {
      let rowIndex = copy.indexOf(items[i]);
      copy.splice(rowIndex, 1);
    }
    super.rows = copy;
    super.count = copy.length;
    super.selected = [];
  }

  buttonMoveUp() {
    this.moveAction(this.selected[0], -1);
  }

  moveAction(row, step: number = 1 | -1) {
    let rowIndex = this.rows.indexOf(row);
    this.moveInternal(rowIndex, step);
    setTimeout(() => {
      let rowIndex = this.rows.indexOf(row);
      document.getElementById('pluginRow' + (rowIndex) + '_id').click();
    }, 50);
  }

  private moveInternal(rowNumber, step: number = -1 | 1) {
    if ((step == -1 && rowNumber < 1) || (step == 1 && rowNumber > this.rows.length - 1)) {
      return;
    }

    let array = this.rows.slice();
    let move = array[rowNumber];
    array[rowNumber] = array[rowNumber + step];
    array[rowNumber + step] = move;

    super.rows = array.slice();
    super.count = this.rows.length;
    this.rowNumber = this.rowNumber + step;

    this.enableMoveUp = !(rowNumber == 0 && step == -1);
    this.enableMoveDown = !(rowNumber == this.rows.length - 1 && step == 1);

    this.setDirty(true);
  }

  buttonMoveDown() {
    this.moveAction(this.selected[0], 1);
  }

  onSelect({selected}) {
    this.rowNumber = this.rows.indexOf(this.selected[0]);

    this.enableMoveDown = selected.length == 1 && this.rowNumber < this.rows.length - 1;
    this.enableMoveUp = selected.length == 1 && this.rowNumber > 0;
    this.enableDelete = selected.length > 0;
    this.enableEdit = selected.length == 1;
  }

  isDirty(): boolean {
    return this.isChanged;
    // return this.enableCancel;
  }

  setDirty(itemValue: boolean) {
    super.isChanged = this.isChanged || itemValue;
    this.enableSave = this.isChanged;
    this.enableCancel = this.isChanged;
  }

  canCancel() {
    return this.enableCancel;
  }

  canSave() {
    return this.enableSave;
  }

  canAdd() {
    return true;
  }

  canEdit() {
    return this.enableEdit;
  }

  canDelete() {
    return this.enableDelete;
  }

  private findRowIndexWithSameRoutingCriteria(backendEntry: BackendFilterEntry): number {
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

  private disableSelectionAndButtons() {
    super.selected = [];
    this.enableMoveDown = false;
    this.enableMoveUp = false;
    this.enableCancel = false;
    this.enableSave = false;
    this.enableEdit = false;
    this.enableDelete = false;
  }
}
