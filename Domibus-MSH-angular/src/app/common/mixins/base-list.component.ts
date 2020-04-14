import {AlertService} from '../alert/alert.service';
import {DownloadService} from '../download.service';
import {OnInit} from '@angular/core';
import {ColumnPickerBase} from '../column-picker/column-picker-base';
import {IBaseList} from './ibase-list';
import {instanceOfFilterableList, instanceOfModifiableList} from './type.utils';
import {HttpClient, HttpParams} from '@angular/common/http';
import {PropertiesService} from '../../properties/support/properties.service';

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
  private propertiesService: PropertiesService;
  private csvMaxCount: number;

  constructor(protected alertService: AlertService, private http: HttpClient) {
    this.columnPicker = new ColumnPickerBase();
  }

  async ngOnInit() {
    this.rows = [];
    this.selected = [];
    this.count = 0;
    this.isLoading = false;
    await this.readDomibusProperties();
  }

  private async readDomibusProperties() {
    this.propertiesService = this.alertService.injector.get(PropertiesService);
    const res = await this.propertiesService.getCsvMaxRowsProperty();
    this.csvMaxCount = +res.value;
  }

  public get name(): string {
    return this.constructor.name;
  }

  protected get GETUrl(): string {
    return undefined;
  }

  protected createAndSetParameters(): HttpParams {
    return new HttpParams();
  }

  public async getServerData(): Promise<any> {
    let getParams = this.createAndSetParameters();
    return this.http.get<any>(this.GETUrl, {params: getParams})
      .toPromise();
  }

  public async setServerResults(data: any) {
  }

  protected async onBeforeGetData(): Promise<any> {
    return true;
  }

  public async getDataAndSetResults(): Promise<any> {
    await this.onBeforeGetData();

    let data = await this.getServerData();
    this.setServerResults(data);
  }

  public async loadServerData(): Promise<any> {
    if (this.isLoading) {
      return null;
    }

    this.isLoading = true;
    this.selected.length = 0;

    if (instanceOfFilterableList(this)) {
      this.resetFilters();
    }

    let result: any;
    try {
      result = await this.getDataAndSetResults();
    } catch (error) {
      this.isLoading = false;
      this.onLoadDataError(error);
      return Promise.reject(error);
    }

    this.isLoading = false;

    if (instanceOfModifiableList(this)) {
      this.isChanged = false;
      this.selected = [];
    }

    return result;
  }

  protected onLoadDataError(error) {
    this.alertService.exception(`Error loading data for '${this.name}' component:`, error);
    error.handled = true;
  }

  public get csvUrl(): string {
    return undefined;
  }

  public async saveAsCSV() {
    if (instanceOfModifiableList(this)) {
      await this.saveIfNeeded();
    }

    if (this.count > this.csvMaxCount) {
      this.alertService.error('Could not downloas as CSV because the maximum accepted number of rows was exceeded.');
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

  public onSelect(event) {

  }

  public onActivate(event) {
    if ('dblclick' === event.type) {
      if (instanceOfModifiableList(this)) {
        this.edit(event.row);
      } else {
        this.showDetails(event.row);
      }
    }
  }

  public showDetails(row: T) {
  }

  isBusy(): boolean {
    return this.isLoading;
  }

};


