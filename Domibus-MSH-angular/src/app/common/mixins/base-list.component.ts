import {AlertComponent} from '../alert/alert.component';
import {AlertService} from '../alert/alert.service';
import {DownloadService} from '../download.service';
import {OnInit} from '@angular/core';
import {ColumnPickerBase} from '../column-picker/column-picker-base';
import {IBaseList} from './ibase-list';
import {instanceOfFilterableList, instanceOfModifiableList, instanceOfPageableList, instanceOfSortableList} from './type.utils';
import {PaginationType} from './Ipageable-list';
import {ErrorLogResult} from '../../errorlog/errorlogresult';
import {HttpClient, HttpParams} from '@angular/common/http';

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * Base class for list components: mainly takes care of getting data from the server
 */
export interface Constructable {
  new(...args);
}

export function ConstructableDecorator(constructor: Constructable) {
}

@ConstructableDecorator
export default class BaseListComponent<T> implements IBaseList<T>, OnInit {
  public rows: T[];
  public selected: T[];
  public count: number;
  public columnPicker: ColumnPickerBase;
  public isLoading: boolean;
  protected GETParams: HttpParams;

  constructor(private alertService: AlertService, private http: HttpClient) {
    this.columnPicker = new ColumnPickerBase();
  }

  ngOnInit(): void {
    this.rows = [];
    this.selected = [];
    this.count = 0;
    this.isLoading = false;
    this.GETParams = new HttpParams();
  }

  public get name(): string {
    return this.constructor.name;
  }

  protected get GETUrl(): string {
    return undefined;
  }

  protected onSetParameters() {
  }

  public async getServerData(): Promise<any> {
    this.GETParams = new HttpParams();
    this.onSetParameters();
    return this.http.get<ErrorLogResult>(this.GETUrl, {params: this.GETParams})
      .toPromise();
  }

  public async setServerResults(data: any) {
  }

  public async getDataAndSetResults(): Promise<any> {
    return this.getServerData()
      .then((data: any) => {
        this.setServerResults(data);
      });
  }

  public async loadServerData(): Promise<any> {
    if (this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.selected.length = 0;

    if (instanceOfFilterableList(this)) {
      this.resetFilters();
    }

    return this.getDataAndSetResults()
      .then((result) => {
        this.isLoading = false;

        if (instanceOfModifiableList(this)) {
          this.isChanged = false;
          this.selected = [];
        }

        if (instanceOfPageableList(this)) {
          this.offset = 0;
        }
      }, (error: any) => {
        this.isLoading = false;
        this.alertService.exception(`Error loading data for '${this.name}' component:`, error);
      });
  }

  public get csvUrl(): string {
    return undefined;
  }

  public async saveAsCSV() {
    if (instanceOfModifiableList(this)) {
      await this.saveIfNeeded();
    }

    if (this.count > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
      return;
    }

    if (instanceOfFilterableList(this)) {
      this.resetFilters();
    }

    DownloadService.downloadNative(this.csvUrl);
  }

  protected hasMethod(name: string) {
    return this[name] && this[name] instanceof Function;
  }

};



