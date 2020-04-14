import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {MatDialog} from '@angular/material';
import {PmodeUploadComponent} from '../pmode-upload/pmode-upload.component';
import * as FileSaver from 'file-saver';
import {DirtyOperations} from 'app/common/dirty-operations';
import {DateFormatService} from 'app/common/customDate/dateformat.service';
import {DialogsService} from '../../common/dialogs/dialogs.service';
import {ApplicationService} from '../../common/application.service';

@Component({
  moduleId: module.id,
  templateUrl: 'currentPMode.component.html',
  providers: [],
  styleUrls: ['./currentPMode.component.css']
})

/**
 * PMode Component Typescript
 */
export class CurrentPModeComponent implements OnInit, DirtyOperations {
  static readonly PMODE_URL: string = 'rest/pmode';
  static readonly PMODE_CSV_URL: string = CurrentPModeComponent.PMODE_URL + '/csv';

  private ERROR_PMODE_EMPTY = 'As PMode is empty, no file was downloaded.';

  public pModeExists = false;
  private pModeContents = '';
  private pModeContentsDirty = false;

  current: any;

  deleteList = [];

  /**
   * Constructor
   * @param {Http} http Http object used for the requests
   * @param {AlertService} alertService Alert Service object used for alerting success and error messages
   * @param {MatDialog} dialog Object used for opening dialogs
   */
  constructor(private applicationService: ApplicationService, private http: HttpClient, private alertService: AlertService,
              public dialog: MatDialog, private dialogsService: DialogsService) {
  }

  /**
   * NgOnInit method
   */
  ngOnInit() {
    this.getCurrentEntry();
  }

  /**
   * Gets the current PMode entry
   */
  getCurrentEntry() {
    this.pModeContentsDirty = false;
    this.http.get(CurrentPModeComponent.PMODE_URL + '/current').subscribe(res => {
      if (res) {
        this.current = res;
        this.getActivePMode();
      } else {
        this.current = null;
        this.pModeExists = false;
      }
    })
  }

  /**
   * Get Request for the Active PMode XML
   */
  getActivePMode() {
    if (this.current && this.current.id) {
      this.pModeContentsDirty = false;
      this.http.get(CurrentPModeComponent.PMODE_URL + '/' + this.current.id + '?noAudit=true', {
        observe: 'response',
        responseType: 'text'
      }).subscribe(res => {
        const HTTP_OK = 200;
        if (res.status === HTTP_OK) {
          this.pModeExists = true;
          this.pModeContents = res.body;
        }
      }, () => {
        this.pModeExists = false;
      })
    }
  }

  /**
   * Method called when Upload button is clicked
   */
  upload() {
    this.dialog.open(PmodeUploadComponent)
      .afterClosed().subscribe(() => {
      this.getCurrentEntry();
    });
  }

  /**
   * Method called when Download button or icon is clicked
   * @param id The id of the selected entry on the DB
   */
  download(pmode) {
    if (this.pModeExists) {
      this.http.get(CurrentPModeComponent.PMODE_URL + '/' + pmode.id, {observe: 'response', responseType: 'text'}).subscribe(res => {
        const uploadDateStr = DateFormatService.format(new Date(pmode.configurationDate));
        CurrentPModeComponent.downloadFile(res.body, uploadDateStr);
      }, err => {
        this.alertService.exception('Error downloading PMode:', err);
      });
    } else {
      this.alertService.error(this.ERROR_PMODE_EMPTY);
    }
  }

  /**
   * Method called when 'Save' button is clicked
   */
  save() {
    this.uploadPmodeContent();
  }

  private uploadPmodeContent() {
    if (!this.pModeContents || !this.pModeContents.trim()) {
      this.alertService.error('Cannot save an empty pMode!');
      return;
    }
    this.dialog.open(PmodeUploadComponent, {
      data: {pModeContents: this.pModeContents}
    }).afterClosed().subscribe(result => {
      if (result && result.done) {
        this.getCurrentEntry();
      }
    });
  }

  /**
   * Method called when 'Cancel' button is clicked
   */
  async cancel() {
    const cancel = await this.dialogsService.openCancelDialog();
    if (cancel) {
      this.getCurrentEntry();
    }
  }

  /**
   * Method that checks if 'Save' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canSave(): boolean {
    return this.pModeExists && this.pModeContentsDirty
      && (!!this.pModeContents && !!this.pModeContents.trim());
  }

  /**
   * Method that checks if 'Cancel' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canCancel(): boolean {
    return this.pModeExists && this.pModeContentsDirty;
  }

  /**
   * Method that checks if 'Upload' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canUpload(): boolean {
    return !this.pModeExists || !this.pModeContentsDirty;
  }

  /**
   * Method that checks if 'Download' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canDownload(): boolean {
    return this.pModeExists && !this.pModeContentsDirty;
  }

  /**
   * Method called when the pmode text is changed by the user
   */
  textChanged() {
    this.pModeContentsDirty = true;
  }


  /**
   * Downloader for the XML file
   * @param data
   */
  private static downloadFile(data: any, date: string) {
    const blob = new Blob([data], {type: 'text/xml'});
    let filename = 'PMode';
    if (date !== '') {
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
    return this.canCancel();
  }
}

