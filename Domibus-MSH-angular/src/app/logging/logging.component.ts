import {ChangeDetectorRef, Component, ElementRef, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LoggingLevelResult} from './logginglevelresult';
import {AlertService} from '../common/alert/alert.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';

/**
 * @author Catalin Enache
 * @since 4.1
 */
@Component({
  moduleId: module.id,
  templateUrl: 'logging.component.html',
  providers: [],
})

export class LoggingComponent extends mix(BaseListComponent).with(FilterableListMixin) implements OnInit {
  static readonly LOGGING_URL: string = 'rest/logging/loglevel';
  static readonly RESET_LOGGING_URL: string = 'rest/logging/reset';

  columnPicker: ColumnPickerBase = new ColumnPickerBase()
  rowLimiter: RowLimiterBase = new RowLimiterBase()

  @ViewChild('rowWithToggleTpl', {static: false}) rowWithToggleTpl: TemplateRef<any>;

  levels: Array<String>;

  loading: boolean = false;

  offset: number = 0;
  orderBy: string = 'loggerName';
  asc: boolean = false;

  constructor(private elementRef: ElementRef, private http: HttpClient, private alertService: AlertService,
              private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    this.search();
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Logger Name',
        prop: 'name'
      },
      {
        cellTemplate: this.rowWithToggleTpl,
        name: 'Logger Level'
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Logger Name', 'Logger Level'].indexOf(col.name) != -1
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  createSearchParams(): HttpParams {
    let searchParams = new HttpParams();

    if (this.orderBy) {
      searchParams = searchParams.append('orderBy', this.orderBy);
    }
    if (this.asc != null) {
      searchParams = searchParams.append('asc', this.asc.toString());
    }

    if (this.filter.loggerName) {
      searchParams = searchParams.append('loggerName', this.activeFilter.loggerName);
    }
    if (this.filter.showClasses) {
      searchParams = searchParams.append('showClasses', this.activeFilter.showClasses);
    }

    return searchParams;
  }

  getLoggingEntries(offset: number, pageSize: number): Observable<LoggingLevelResult> {
    let searchParams = this.createSearchParams();

    searchParams = searchParams.append('page', offset.toString());
    searchParams = searchParams.append('pageSize', pageSize.toString());

    return this.http.get<LoggingLevelResult>(LoggingComponent.LOGGING_URL, {params: searchParams});
  }

  page() {
    this.loading = true;
    super.resetFilters();
    this.getLoggingEntries(this.offset, this.rowLimiter.pageSize).subscribe((result: LoggingLevelResult) => {

      this.rowLimiter.pageSize = this.rowLimiter.pageSize;
      super.count = result.count;
      super.rows = result.loggingEntries;

      this['filter'] = result.filter;
      this.levels = result.levels;

      this.loading = false;
    }, (error: any) => {
      this.loading = false;
      this.alertService.exception('Error occurred:', error);
    });

  }

  onPage(event) {
    this.offset = event.offset;
    this.page();
  }

  onSort(event) {
    this.orderBy = event.column.prop;
    this.asc = (event.newValue === 'desc') ? false : true;

    this.page();
  }

  changePageSize(newPageLimit: number) {
    super.resetFilters();
    this.offset = 0;
    this.rowLimiter.pageSize = newPageLimit;
    this.page();
  }

  onLevelChange(newLevel: string, row: any) {
    if (newLevel !== row.level) {
      this.alertService.clearAlert();
      this.http.post(LoggingComponent.LOGGING_URL, {
        name: row.name,
        level: newLevel,
      }, {headers: this.headers}).subscribe(
        () => {
          this.page();
        },
        error => {
          this.alertService.exception('An error occurred while setting logging level: ', error);
          this.loading = false;
        }
      );
    }
  }

  resetLogging() {
    console.log('Reset button clicked!');
    this.http.post(LoggingComponent.RESET_LOGGING_URL, {}).subscribe(
      res => {
        this.alertService.success('Logging configuration was successfully reset.', false);
        this.page();
      },
      error => {
        this.alertService.exception('An error occurred while resetting logging: ', error);
        this.loading = false;
      }
    );
  }

  search() {
    console.log('Searching using filter:', this.filter);
    // super.setActiveFilter();
    this.offset = 0;
    this.page();
  }

}
