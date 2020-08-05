import {Component, TemplateRef, ViewChild, Renderer2, ElementRef, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Http, Response, URLSearchParams} from '@angular/http';
import {ErrorLogResult} from './errorlogresult';
import {AlertService} from '../common/alert/alert.service';
import {ErrorlogDetailsComponent} from 'app/errorlog/errorlog-details/errorlog-details.component';
import {MdDialog, MdDialogRef} from '@angular/material';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {DownloadService} from '../common/download.service';
import {AlertComponent} from '../common/alert/alert.component';
import {Md2Datepicker} from 'md2';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import SortableListMixin from '../common/mixins/sortable-list.mixin';

@Component({
  moduleId: module.id,
  templateUrl: 'errorlog.component.html',
  providers: [],
  styleUrls: ['./errorlog.component.css']
})

export class ErrorLogComponent extends mix(BaseListComponent).with(FilterableListMixin, SortableListMixin) implements OnInit {

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  public rowLimiter: RowLimiterBase;

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  @ViewChild('rowWithDateFormatTpl') rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rawTextTpl') public rawTextTpl: TemplateRef<any>;

  timestampFromMaxDate: Date = new Date();
  timestampToMinDate: Date = null;
  timestampToMaxDate: Date = new Date();

  notifiedFromMaxDate: Date = new Date();
  notifiedToMinDate: Date = null;
  notifiedToMaxDate: Date = new Date();

  loading: boolean = false;
  rows = [];
  count: number = 0;
  offset: number = 0;

  mshRoles: Array<String>;
  errorCodes: Array<String>;

  advancedSearch: boolean;

  static readonly ERROR_LOG_URL: string = 'rest/errorlogs';
  static readonly ERROR_LOG_CSV_URL: string = ErrorLogComponent.ERROR_LOG_URL + '/csv?';

  constructor(private elementRef: ElementRef, private http: Http, private alertService: AlertService, public dialog: MdDialog, private renderer: Renderer2) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    this.columnPicker.allColumns = [
      {
        name: 'Signal Message Id',
        prop: 'errorSignalMessageId'
      },
      {
        name: 'AP Role',
        prop: 'mshRole',
        width: 50
      },
      {
        name: 'Message Id',
        cellTemplate: this.rawTextTpl,
        prop: 'messageInErrorId',
      },
      {
        name: 'Error Code',
        width: 50
      },
      {
        name: 'Error Detail',
        cellTemplate: this.rawTextTpl,
        width: 350
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Timestamp',
        width: 180
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Notified'
      }

    ];
    this.rowLimiter = new RowLimiterBase();

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Message Id', 'Error Code', 'Timestamp'].indexOf(col.name) != -1
    });

    this.orderBy = 'timestamp';
    this.asc = false;

    this.search();
  }

  createSearchParams(): URLSearchParams {
    const searchParams = new URLSearchParams();

    if (this.orderBy) {
      searchParams.set('orderBy', this.orderBy);
    }
    if (this.asc != null) {
      searchParams.set('asc', this.asc.toString());
    }

    if (this.activeFilter.errorSignalMessageId) {
      searchParams.set('errorSignalMessageId', this.activeFilter.errorSignalMessageId);
    }
    if (this.activeFilter.mshRole) {
      searchParams.set('mshRole', this.activeFilter.mshRole);
    }
    if (this.activeFilter.messageInErrorId) {
      searchParams.set('messageInErrorId', this.activeFilter.messageInErrorId);
    }
    if (this.activeFilter.errorCode) {
      searchParams.set('errorCode', this.activeFilter.errorCode);
    }
    if (this.activeFilter.errorDetail) {
      searchParams.set('errorDetail', this.activeFilter.errorDetail);
    }
    if (this.activeFilter.timestampFrom != null) {
      searchParams.set('timestampFrom', this.activeFilter.timestampFrom.getTime());
    }
    if (this.filter.timestampTo != null) {
      searchParams.set('timestampTo', this.activeFilter.timestampTo.getTime());
    }
    if (this.activeFilter.notifiedFrom != null) {
      searchParams.set('notifiedFrom', this.activeFilter.notifiedFrom.getTime());
    }
    if (this.activeFilter.notifiedTo != null) {
      searchParams.set('notifiedTo', this.activeFilter.notifiedTo.getTime());
    }

    return searchParams;
  }

  getErrorLogEntries(offset: number, pageSize: number): Observable<ErrorLogResult> {
    const searchParams = this.createSearchParams();

    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());

    return this.http.get(ErrorLogComponent.ERROR_LOG_URL, {
      search: searchParams
    }).map((response: Response) =>
      response.json()
    );
  }

  page(offset, pageSize) {
    this.loading = true;
    this.getErrorLogEntries(offset, pageSize).subscribe((result: ErrorLogResult) => {
      this.offset = offset;
      this.rowLimiter.pageSize = pageSize;
      this.count = result.count;

      const start = offset * pageSize;
      const end = start + pageSize;
      const newRows = [...result.errorLogEntries];

      let index = 0;
      for (let i = start; i < end; i++) {
        newRows[i] = result.errorLogEntries[index++];
      }

      this.rows = newRows;

      if (result.filter.timestampFrom != null) {
        result.filter.timestampFrom = new Date(result.filter.timestampFrom);
      }
      if (result.filter.timestampTo != null) {
        result.filter.timestampTo = new Date(result.filter.timestampTo);
      }
      if (result.filter.notifiedFrom != null) {
        result.filter.notifiedFrom = new Date(result.filter.notifiedFrom);
      }
      if (result.filter.notifiedTo != null) {
        result.filter.notifiedTo = new Date(result.filter.notifiedTo);
      }

      this.filter = result.filter;
      this.mshRoles = result.mshRoles;
      this.errorCodes = result.errorCodes;

      this.loading = false;
    }, (error: any) => {
      console.log('error getting the error log:' + error);
      this.loading = false;
      this.alertService.exception('Error occured:', error);
    });

  }

  onPage(event) {
    super.resetFilters();
    this.page(event.offset, event.pageSize);
  }

  /**
   * The method is an override of the abstract method defined in SortableList mixin
   */
  public reload () {
    this.page(0, this.rowLimiter.pageSize);
  }

  /**
   * The method is an override of the abstract method defined in SortableList mixin
   */
  public onBeforeSort () {
    super.resetFilters();
  }

  changePageSize(newPageLimit: number) {
    super.resetFilters();
    this.page(0, newPageLimit);
  }

  search() {
    console.log('Searching using filter:' + this.filter);
    this.setActiveFilter();
    this.page(0, this.rowLimiter.pageSize);
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  onNotifiedFromChange(event) {
    this.notifiedToMinDate = event.value;
  }

  onNotifiedToChange(event) {
    this.notifiedFromMaxDate = event.value;
  }

  toggleAdvancedSearch(): boolean {
    this.advancedSearch = !this.advancedSearch;
    return false;//to prevent default navigation
  }

  onActivate(event) {
    if ('dblclick' === event.type) {
      this.details(event.row);
    }
  }

  details(selectedRow: any) {
    let dialogRef: MdDialogRef<ErrorlogDetailsComponent> = this.dialog.open(ErrorlogDetailsComponent);
    dialogRef.componentInstance.message = selectedRow;
    // dialogRef.componentInstance.currentSearchSelectedSource = this.currentSearchSelectedSource;
    dialogRef.afterClosed().subscribe(result => {
      //Todo:
    });
  }

  saveAsCSV() {
    if (this.count > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
      return;
    }
    super.resetFilters();
    DownloadService.downloadNative(ErrorLogComponent.ERROR_LOG_CSV_URL + this.createSearchParams().toString());
  }

  onClick(event) {
    console.log(event);
  }

}
