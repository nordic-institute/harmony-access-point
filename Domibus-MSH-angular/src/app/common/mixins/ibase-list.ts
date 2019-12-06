import {ColumnPickerBase} from '../column-picker/column-picker-base';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the base List
 * */
export interface IBaseList<T> {
  rows: Array<T>;
  count: number;
  readonly csvUrl: string;
  columnPicker: ColumnPickerBase; //here for now
}
