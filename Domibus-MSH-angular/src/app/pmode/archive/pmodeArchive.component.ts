import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {MatDialog} from '@angular/material';
import {PmodeUploadComponent} from '../pmode-upload/pmode-upload.component';
import * as FileSaver from 'file-saver';
import {ActionDirtyDialogComponent} from 'app/pmode/action-dirty-dialog/action-dirty-dialog.component';
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
import {ApplicationContextService} from '../../common/application-context.service';

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
  implements OnInit, AfterViewInit, AfterViewChecked {

  static readonly PMODE_URL: string = 'rest/pmode';
  static readonly PMODE_CSV_URL: string = PModeArchiveComponent.PMODE_URL + '/csv';

  @ViewChild('descriptionTpl', {static: false}) public descriptionTpl: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl', {static: false}) public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;

  deleteList: any[];

  currentDomain: Domain;
  currentPMode: any;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private alertService: AlertService,
              public dialog: MatDialog, private dialogsService: DialogsService,
              private domainService: DomainService, private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    this.currentPMode = null;

    this.deleteList = [];

    this.loadServerData();

    this.domainService.getCurrentDomain()
      .subscribe((domain: Domain) => this.currentDomain = domain);
  }

  public get name(): string {
    return 'pModes';
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

  getResultObservable(): Observable<any[]> {
    return this.http.get<any[]>(PModeArchiveComponent.PMODE_URL + '/list');
  }

  public async getDataAndSetResults(): Promise<any> {
    this.deleteList.length = 0;
    return this.getAllPModeEntries();
  }

  async getAllPModeEntries(): Promise<any> {
    return this.getResultObservable().toPromise().then((results) => {
      super.rows = results;
      super.count = this.rows.length;

      this.currentPMode = null;

      if (this.count > 0) {
        this.currentPMode = this.rows[0];
        this.currentPMode.current = true;
        this.currentPMode.description = '[CURRENT]: ' + this.currentPMode.description;
      }
    });
  }

  doSave(): Promise<any> {
    return this.deleteEntries();
  }

  private async deleteEntries(): Promise<any> {
    const queryParams = {ids: this.deleteList};

    let result;
    try {
      result = await this.http.delete(PModeArchiveComponent.PMODE_URL, {params: queryParams}).toPromise();
      this.deleteList = [];
      super.isChanged = false;
    } catch (error) {
      this.alertService.exception('The operation \'delete pmodes\' not completed successfully.', error);
      super.selected = [];
      error.handled = true;
      throw error;
    }
    return result;
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
      super.isChanged = true;
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
  async restoreArchive(selectedRow) {
    if (!this.isDirty()) {
      const restore = await this.dialogsService.openRestoreDialog();
      if (restore) {
        this.restore(selectedRow);
      }
    } else {
      const option = await this.openUserDialog();
      if (option === 'restore') {
        await this.restore(selectedRow);
      } else if (option === 'ok') {
        await this.deleteEntries().then(() => this.restore(selectedRow));
      }
    }
  }

  private openUserDialog() {
    return this.dialog.open(ActionDirtyDialogComponent, {
      data: {
        actionTitle: 'You will now also Restore an older version of the PMode',
        actionName: 'restore',
        actionIconName: 'settings_backup_restore'
      }
    }).afterClosed().toPromise();
  }

  private async restore(selectedRow) {
    super.isSaving = true;
    this.currentPMode.current = false;
    try {
      const res = await this.http.put(PModeArchiveComponent.PMODE_URL + '/restore/' + selectedRow.id, null).toPromise();
      this.alertService.success(res);

      this.deleteList = [];
      super.selected = [];
      super.isSaving = false;

      await this.getAllPModeEntries(); // todo better call loadServerData??
    } catch (e) {
      super.isSaving = false;
      this.alertService.exception('The operation \'restore pmode\' not completed successfully.', e);
    }
  }

  private uploadPmode() {
    this.dialog.open(PmodeUploadComponent)
      .afterClosed().subscribe(result => {
      this.getAllPModeEntries();
    });
  }

  /**
   * Method called when Download button or icon is clicked
   * @param id The id of the selected entry on the DB
   */
  download(row) {
    this.http.get(PModeArchiveComponent.PMODE_URL + '/' + row.id, + '?archiveAudit=true', {observe: 'response', responseType: 'text'}).subscribe(res => {
      const uploadDateStr = DateFormatService.format(new Date(row.configurationDate));
      this.downloadFile(res.body, this.currentDomain.name, uploadDateStr);
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
  private downloadFile(data: any, domain: string, date: string) {
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

  canDelete() {
    return this.atLeastOneRowSelected() && !this.currentPModeSelected() && !this.isBusy();
  }

  canDownload() {
    return this.oneRowSelected() && !this.isBusy();
  }

  canRestore() {
    return this.oneRowSelected() && !this.currentPModeSelected() && !this.isBusy();
  }

  private oneRowSelected() {
    return this.selected.length === 1;
  }

  private currentPModeSelected() {
    return this.selected.find(el => el.id === this.currentPMode.id);
  }

  private atLeastOneRowSelected() {
    return this.selected.length > 0;
  }

}


