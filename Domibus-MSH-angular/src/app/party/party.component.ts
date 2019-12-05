import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material';
import {RowLimiterBase} from 'app/common/row-limiter/row-limiter-base';
import {ColumnPickerBase} from 'app/common/column-picker/column-picker-base';
import {PartyService} from './party.service';
import {CertificateRo, PartyFilteredResult, PartyResponseRo, ProcessRo} from './party';
import {AlertService} from '../common/alert/alert.service';
import {PartyDetailsComponent} from './party-details/party-details.component';
import {DirtyOperations} from '../common/dirty-operations';
import {CurrentPModeComponent} from '../pmode/current/currentPMode.component';
import {HttpClient} from '@angular/common/http';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import ModifiableListMixin from '../common/mixins/modifiable-list.mixin';
import {DialogsService} from '../common/dialogs/dialogs.service';

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Component({
  selector: 'app-party',
  providers: [PartyService],
  templateUrl: './party.component.html',
  styleUrls: ['./party.component.css']
})

export class PartyComponent extends mix(BaseListComponent).with(FilterableListMixin, ModifiableListMixin) implements OnInit, DirtyOperations {
  // rows: PartyResponseRo[];
  allRows: PartyResponseRo[];
  selected: PartyResponseRo[];

  rowLimiter: RowLimiterBase = new RowLimiterBase();
  columnPicker: ColumnPickerBase = new ColumnPickerBase();

  offset: number;
  // count: number;
  loading: boolean;

  newParties: PartyResponseRo[];
  updatedParties: PartyResponseRo[];
  deletedParties: PartyResponseRo[];

  allProcesses: string[];

  pModeExists: boolean;
  isBusy: boolean;

  constructor(public dialog: MatDialog, private dialogsService: DialogsService, public partyService: PartyService,
              public alertService: AlertService, private http: HttpClient, private changeDetector: ChangeDetectorRef) {
    super();
  }

  async ngOnInit() {
    super.ngOnInit();

    this.isBusy = false;
    // this.rows = [];
    this.allRows = [];
    this.selected = [];

    this.offset = 0;
    // this.count = 0;
    this.loading = false;

    this.newParties = [];
    this.updatedParties = [];
    this.deletedParties = [];

    const res = await this.http.get<any>(CurrentPModeComponent.PMODE_URL + '/current').toPromise();
    if (res) {
      this.pModeExists = true;
      this.search();
    } else {
      this.pModeExists = false;
    }
  }

  ngAfterViewInit() {
    this.initColumns();
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  isDirty(): boolean {
    return this.newParties.length + this.updatedParties.length + this.deletedParties.length > 0;
  }

  resetDirty() {
    this.newParties.length = 0;
    this.updatedParties.length = 0;
    this.deletedParties.length = 0;
  }

  private search() {
    // super.setActiveFilter();
    this.listPartiesAndProcesses();
  }

  async listPartiesAndProcesses() {
    this.offset = 0;
    var promises: [Promise<PartyFilteredResult>, Promise<ProcessRo[]>] = [
      this.partyService.listParties(this.activeFilter.name, this.activeFilter.endPoint, this.activeFilter.partyID, this.activeFilter.process, this.activeFilter.process_role).toPromise(),
      this.partyService.listProcesses().toPromise()
    ];

    try {
      let data = await Promise.all(promises);
      const partiesRes: PartyFilteredResult = data[0];
      const processes: ProcessRo[] = data[1];

      this.allProcesses = processes.map(el => el.name);

      super.rows = partiesRes.data;
      this.allRows = partiesRes.allData;
      super.count = this.allRows.length;
      this.selected.length = 0;

      this.loading = false;
      this.resetDirty();

    } catch (error) {
      this.alertService.exception('Could not load parties due to: ', error);
      this.loading = false;
    }
  }

  initColumns() {
    this.columnPicker.allColumns = [
      {
        name: 'Party Name',
        prop: 'name',
        width: 10
      },
      {
        name: 'End Point',
        prop: 'endpoint',
        width: 150
      },
      {
        name: 'Party Id',
        prop: 'joinedIdentifiers',
        width: 10
      },
      {
        name: 'Process (I=Initiator, R=Responder, IR=Both)',
        prop: 'joinedProcesses',
        width: 200
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['name', 'endpoint', 'joinedIdentifiers', 'joinedProcesses'].indexOf(col.prop) !== -1
    })
  }

  changePageSize(newPageLimit: number) {
    super.resetFilters();
    this.offset = 0;
    this.rowLimiter.pageSize = newPageLimit;
  }

  onPageChange(event: any) {
    super.resetFilters();
    this.offset = event.offset;
  }

  public get csvUrl(): string {
    return PartyService.CSV_PARTIES
      + this.partyService.getFilterPath(this.activeFilter.name, this.activeFilter.endPoint, this.activeFilter.partyID, this.activeFilter.process);
  }

  onActivate(event) {
    if ('dblclick' === event.type) {
      this.edit(event.row);
    }
  }

  canAdd() {
    return !!this.pModeExists && !this.isBusy;
  }

  canSave() {
    return this.isDirty() && !this.isBusy;
  }

  canEdit() {
    return !!this.pModeExists && this.selected.length === 1 && !this.isBusy;
  }

  canCancel() {
    return this.isDirty() && !this.isBusy;
  }

  canDelete() {
    return !!this.pModeExists && this.selected.length === 1 && !this.isBusy;
  }

  async cancel() {
    if (this.isBusy) return;
    const cancel = await this.dialogsService.openCancelDialog();
    if (cancel) {
      super.resetFilters();
      this.listPartiesAndProcesses();
    }
  }

  async save(): Promise<boolean> {
    if (this.isBusy) return;
    const save = await this.dialogsService.openSaveDialog();
    if (save) {
      try {
        this.partyService.validateParties(this.rows)
      } catch (err) {
        this.alertService.exception('Party validation error:', err, false);
        return false;
      }

      this.isBusy = true;
      return await this.partyService.updateParties(this.rows).then(() => {
        this.resetDirty();
        this.isBusy = false;
        this.alertService.success('Parties saved successfully.', false);
        return true;
      }).catch(err => {
        this.isBusy = false;
        this.alertService.exception('Party update error:', err, false);
        return false;
      });
    } else {
      return false;
    }
  }

  async add() {
    if (this.isBusy) return;

    const newParty = this.partyService.initParty();
    this.rows.push(newParty);
    this.allRows.push(newParty);

    this.selected.length = 0;
    this.selected.push(newParty);
    super.count++;

    this.newParties.push(newParty);
    const ok = await this.edit(newParty);
    if (!ok) {
      this.remove();
    }
    super.rows = [...this.rows];
  }

  remove() {
    if (this.isBusy) return;

    const deletedParty = this.selected[0];
    if (!deletedParty) return;

    console.log('removing ', deletedParty)

    this.rows.splice(this.rows.indexOf(deletedParty), 1);
    this.allRows.splice(this.allRows.indexOf(deletedParty), 1);
    super.rows = [...this.rows];

    this.selected.length = 0;
    super.count--;

    if (this.newParties.indexOf(deletedParty) < 0)
      this.deletedParties.push(deletedParty);
    else
      this.newParties.splice(this.newParties.indexOf(deletedParty), 1);
  }

  async edit(row): Promise<boolean> {
    row = row || this.selected[0];

    await this.manageCertificate(row);

    const rowCopy = JSON.parse(JSON.stringify(row)); // clone
    const allProcessesCopy = JSON.parse(JSON.stringify(this.allProcesses));

    const dialogRef: MatDialogRef<PartyDetailsComponent> = this.dialog.open(PartyDetailsComponent, {
      data: {
        edit: rowCopy,
        allProcesses: allProcessesCopy
      }
    });

    const ok = await dialogRef.afterClosed().toPromise();
    if (ok) {
      if (JSON.stringify(row) === JSON.stringify(rowCopy))
        return; // nothing changed

      Object.assign(row, rowCopy);
      row.name = rowCopy.name;// TODO temp
      super.rows = [...this.rows];

      if (this.updatedParties.indexOf(row) < 0)
        this.updatedParties.push(row);
    }

    return ok;
  }

  manageCertificate(party: PartyResponseRo): Promise<CertificateRo> {
    return new Promise((resolve, reject) => {
      if (!party.certificate) {
        this.partyService.getCertificate(party.name)
          .subscribe((cert: CertificateRo) => {
            party.certificate = cert;
            resolve(cert);
          }, err => {
            resolve(null);
          });
      } else {
        resolve(party.certificate);
      }
    });
  }

  OnSort() {
    super.resetFilters();
  }
}
