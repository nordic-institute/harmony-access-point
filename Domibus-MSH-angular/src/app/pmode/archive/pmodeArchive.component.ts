import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {MatDialog} from '@angular/material';
import {isNullOrUndefined} from 'util';
import {PmodeUploadComponent} from '../pmode-upload/pmode-upload.component';
import * as FileSaver from 'file-saver';
import {ActionDirtyDialogComponent} from 'app/pmode/action-dirty-dialog/action-dirty-dialog.component';
import {DirtyOperations} from 'app/common/dirty-operations';
import {Observable} from 'rxjs/Observable';
import {DateFormatService} from 'app/common/customDate/dateformat.service';
import {PmodeViewComponent} from './pmode-view/pmode-view.component';
import {CurrentPModeComponent} from '../current/currentPMode.component';
import {DomainService} from '../../security/domain.service';
import {Domain} from '../../security/domain';
import {DialogsService} from '../../common/dialogs/dialogs.service';
import mix from '../../common/mixins/mixin.utils';
import BaseListComponent from '../../common/mixins/base-list.component';
import ModifiableListMixin from '../../common/mixins/modifiable-list.mixin';
import {ClientPageableListMixin} from '../../common/mixins/pageable-list.mixin';
import {ApplicationService} from '../../common/application.service';

@Component({
  moduleId: module.id,
  templateUrl: 'pmodeArchive.component.html',
  providers: [],
  styleUrls: ['./pmodeArchive.component.css']
})

/**
 * PMode Archive Component Typescript
 */
export class PModeArchiveComponent extends mix(BaseListComponent)
  .with(ModifiableListMixin, ClientPageableListMixin)
  implements OnInit, DirtyOperations, AfterViewInit, AfterViewChecked {

  static readonly PMODE_URL: string = 'rest/pmode';
  static readonly PMODE_CSV_URL: string = PModeArchiveComponent.PMODE_URL + '/csv';

  @ViewChild('descriptionTpl', {static: false}) public descriptionTpl: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl', {static: false}) public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;

  disabledSave: boolean;
  disabledCancel: boolean;
  disabledDownload: boolean;
  disabledDelete: boolean;
  disabledRestore: boolean;

  actualId: number;
  actualRow: number;

  deleteList: any[];

  currentDomain: Domain;

  // needed for the first request after upload
  // datatable was empty if we don't do the request again
  // resize window shows information

  private uploaded: boolean;

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  /**
   * Constructor
   * @param {Http} http Http object used for the requests
   * @param {AlertService} alertService Alert Service object used for alerting success and error messages
   * @param {MatDialog} dialog Object used for opening dialogs
   */
  constructor(private applicationService: ApplicationService, private http: HttpClient, private alertService: AlertService,
              public dialog: MatDialog, private dialogsService: DialogsService,
              private domainService: DomainService, private changeDetector: ChangeDetectorRef) {
    super();
  }

  /**
   * NgOnInit method
   */
  ngOnInit() {
    super.ngOnInit();

    this.actualId = 0;
    this.actualRow = 0;

    this.deleteList = [];

    this.uploaded = false;

    this.loadServerData();

    this.domainService.getCurrentDomain()
      .subscribe((domain: Domain) => this.currentDomain = domain);
  }

  public get name(): string {
    return 'pmodes';
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Configuration Date',
        sortable: false
      },
      {
        name: 'Username',
        sortable: false
      },
      {
        cellTemplate: this.descriptionTpl,
        name: 'Description',
        sortable: false
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 80,
        sortable: false
      }
    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Configuration Date', 'Username', 'Description', 'Actions'].indexOf(col.name) !== -1
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  /**
   * Gets all the PMode
   * @returns {Observable<any>}
   */
  getResultObservable(): Observable<any[]> {
    return this.http.get<any[]>(PModeArchiveComponent.PMODE_URL + '/list');
  }

  public async getDataAndSetResults(): Promise<any> {
    this.deleteList.length = 0;
    this.disableAllButtons();
    return this.getAllPModeEntries();
  }

  /**
   * Gets all the PModes Entries
   */
  async getAllPModeEntries() {
    return this.getResultObservable().toPromise().then((results) => {
      super.rows = results;
      super.count = this.rows.length;

      this.actualRow = 0;
      this.actualId = undefined;

      if (this.count > 0) {
        this.rows[0].current = true;
        this.rows[0].description = '[CURRENT]: ' + this.rows[0].description;
        this.actualId = this.rows[0].id;
      }
    });
  }

  /**
   * Disable All the Buttons
   * used mainly when no row is selected
   */
  private disableAllButtons() {
    this.disabledSave = true;
    this.disabledCancel = true;
    this.disabledDownload = true;
    this.disabledDelete = true;
    this.disabledRestore = true;
  }

  /**
   * Enable Save and Cancel buttons
   * used when changes occurred (deleted entries)
   */
  private enableSaveAndCancelButtons() {
    this.disabledSave = false;
    this.disabledCancel = false;
    this.disabledDownload = true;
    this.disabledDelete = true;
    this.disabledRestore = true;
  }

  /**
   * Method called by NgxDatatable on selection/deselection
   * @param {any} selected selected/unselected object
   */
  onSelect({selected}) {
    if (isNullOrUndefined(selected) || selected.length === 0) {
      this.disableAllButtons();
      return;
    }

    this.disabledDownload = !(this.selected[0] != null && this.selected.length === 1);
    this.disabledDelete = this.selected.findIndex(sel => sel.id === this.actualId) !== -1;
    this.disabledRestore = !(this.selected[0] != null && this.selected.length === 1 && this.selected[0].id !== this.actualId);
  }

  doSave(): Promise<any> {
    const queryParams = {ids: this.deleteList};
    return this.http.delete(PModeArchiveComponent.PMODE_URL, {params: queryParams}).toPromise()
      .then(() => {
          this.disableAllButtons();
          this.deleteList = [];
        }
      );
  }

  /**
   * Method called when Download button is clicked
   * @param row The selected row
   */
  downloadArchive(row) {
    this.download(row);
  }

  /**
   * Method called when Action Delete icon is clicked
   * @param row Row where Delete icon is located
   */
  deleteArchiveAction(row) {
    this.deleteRows([row]);
  }

  /**
   * Method called when Delete button is clicked
   * All the selected rows will be deleted
   */
  delete() {
    this.deleteRows(this.selected);
  }

  private deleteRows(rows: any[]) {
    for (let i = rows.length - 1; i >= 0; i--) {
      const row = rows[i];
      const rowIndex = this.rows.indexOf(row);
      this.rows.splice(rowIndex, 1);
      this.deleteList.push(row.id);
      super.count = this.count - 1;
    }
    super.rows = [...this.rows];

    setTimeout(() => {
      super.selected = [];
      this.enableSaveAndCancelButtons();
    }, 100);
  }

  /**
   * Method called when Restore button is clicked
   * Restores the PMode for the selected row
   * - Creates a similar entry like @selectedRow
   * - Sets that entry as current
   *
   * @param selectedRow Selected Row
   */
  restoreArchive(selectedRow) {
    if (!this.isDirty()) {
      this.dialogsService.openRestoreDialog().then(restore => {
        if (restore) {
          this.restore(selectedRow);
        }
      });
    } else {
      this.dialog.open(ActionDirtyDialogComponent, {
        data: {
          actionTitle: 'You will now also Restore an older version of the PMode',
          actionName: 'restore',
          actionIconName: 'settings_backup_restore'
        }
      }).afterClosed().subscribe(result => {
        if (result === 'ok') {
          this.http.delete(PModeArchiveComponent.PMODE_URL, {params: {ids: this.deleteList}}).subscribe(result => {
              this.restore(selectedRow);
            },
            error => {
              this.alertService.exception('The operation \'delete pmodes\' not completed successfully.', error);
              this.enableSaveAndCancelButtons();
              super.selected = [];
            });
        } else if (result === 'restore') {
          this.restore(selectedRow);
        }
      });
    }
  }

  private async restore(selectedRow) {
    this.rows[this.actualRow].current = false;
    try {
      let res = await this.http.put(PModeArchiveComponent.PMODE_URL + '/restore/' + selectedRow.id, null)
        .toPromise();
      this.alertService.success(res);

      this.deleteList = [];
      this.disableAllButtons();
      super.selected = [];
      this.actualRow = 0;

      await this.getAllPModeEntries();

    } catch (e) {
      this.alertService.exception('The operation \'restore pmode\' not completed successfully.', e);
    }
  }

  private uploadPmode() {
    this.dialog.open(PmodeUploadComponent)
      .afterClosed().subscribe(result => {
      this.getAllPModeEntries();
    });
    this.uploaded = true;
  }

  /**
   * Method called when Download button or icon is clicked
   * @param id The id of the selected entry on the DB
   */
  download(row) {
    this.http.get(PModeArchiveComponent.PMODE_URL + '/' + row.id, {observe: 'response', responseType: 'text'}).subscribe(res => {
      const uploadDateStr = DateFormatService.format(new Date(row.configurationDate));
      PModeArchiveComponent.downloadFile(res.body, this.currentDomain.name, uploadDateStr);
    }, err => {
      this.alertService.exception('Error downloading pMode from archive:', err);
    });
  }

  get csvUrl(): string {
    return PModeArchiveComponent.PMODE_CSV_URL;
  }

  /**
   * Downloader for the XML file
   * @param data
   * @param domain
   * @param date
   */
  private static downloadFile(data: any, domain: string, date: string) {
    const blob = new Blob([data], {type: 'text/xml'});
    let filename = 'PMode';
    if (domain) {
      filename += '-' + domain;
    }
    if (date) {
      filename += '-' + date;
    }
    filename += '.xml';
    FileSaver.saveAs(blob, filename);
  }

  /**
   * IsDirty method used for the IsDirtyOperations
   * @returns {boolean}
   */
  isDirty(): boolean {
    return !this.disabledCancel;
  }

  edit(row) {
    this.showDetails(row);
  }

  private showDetails(row) {
    this.http.get(CurrentPModeComponent.PMODE_URL + '/' + row.id + '?noAudit=true', {
      observe: 'response',
      responseType: 'text'
    }).subscribe(res => {
      const HTTP_OK = 200;
      if (res.status === HTTP_OK) {
        const content = res.body;
        this.dialog.open(PmodeViewComponent, {
          data: {metadata: row, content: content}
        });
      }
    }, err => {
      this.alertService.exception('Error getting the current PMode:', err);
    });
  }

  canCancel() {
    return !this.disabledCancel;
  }

  canSave() {
    return !this.disabledSave;
  }

  canDelete() {
    return !this.disabledDelete;
  }
}

