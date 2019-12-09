import {AlertComponent} from '../alert/alert.component';
import {AlertService} from '../alert/alert.service';
import {DownloadService} from '../download.service';
import {OnInit} from '@angular/core';
import {ColumnPickerBase} from '../column-picker/column-picker-base';
import {IBaseList} from './ibase-list';
import {instanceOfFilterableList, instanceOfModifiableList, instanceOfPageableList} from './type.utils';
import {PaginationType} from './Ipageable-list';

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * Base class for list components;
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

  constructor(private alertService: AlertService) {
    this.columnPicker = new ColumnPickerBase();
  }

  ngOnInit(): void {
    this.rows = [];
    this.selected = [];
    this.count = 0;
    this.isLoading = false;
  }

  public get name(): string {
    return this.constructor.name;
  }

  public async doGetData(): Promise<any> {
  }

  public async getData(): Promise<any> {
    if (this.isLoading) {
      return;
    }

    this.isLoading = true;

    this.selected.length = 0;
    // this.rows = [];
    // this.count = 0;

    if (instanceOfFilterableList(this)) {
      this.resetFilters();
    }

    return this.doGetData().then((result) => {
      this.isLoading = false;
      if (instanceOfModifiableList(this)) {
        this.isChanged = false;
        this.selected = [];
      }
      //can we do it elsewhere??
      if (instanceOfPageableList(this) && this.type == PaginationType.Client) {
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



