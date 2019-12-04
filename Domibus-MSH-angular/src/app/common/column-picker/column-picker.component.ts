import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {isNullOrUndefined} from 'util';

@Component({
  selector: 'app-column-picker',
  templateUrl: './column-picker.component.html',
  styleUrls: ['./column-picker.component.css']
})
export class ColumnPickerComponent implements OnInit {

  columnSelection: boolean;

  @Input()
  allColumns = [];

  @Input()
  selectedColumns = [];

  @Output()
  onSelectedColumnsChanged = new EventEmitter<Array<any>>();

  constructor() {
  }

  ngOnChanges(changes: SimpleChanges) {

    this.allColumns.forEach(col => col.isSelected = this.isChecked(col));

  }

  ngOnInit() {

  }

  toggleColumnSelection() {
    this.columnSelection = !this.columnSelection
  }

  /*
  * Note: if an 'Actions' column exists, it will be the last one of the array
  * */
  toggle(col) {
    setTimeout(() => {
      this.selectedColumns = this.allColumns.filter(col => col.isSelected);
      this.onSelectedColumnsChanged.emit(this.selectedColumns);
    });

    // const selecting = !this.isChecked(col);
    // col.isSelected = !col.isSelected;

    // if (col.isSelected) {
    //   this.selectedColumns = this.allColumns.filter(c => this.selectedColumns.indexOf(c) >= 0 || c.name === col.name);
    // } else {
    //   this.selectedColumns = this.selectedColumns.filter(c => c.name !== col.name);
    // }
    //
    // this.setLastColumn(this.selectedColumns, 'Actions');
    // this.onSelectedColumnsChanged.emit(this.selectedColumns);


  }

  selectAllColumns() {
    this.selectedColumns = [...this.allColumns];
    this.onSelectedColumnsChanged.emit(this.selectedColumns);
  }

  selectNoColumns() {
    this.selectedColumns = [];
    this.onSelectedColumnsChanged.emit(this.selectedColumns);
  }

  isChecked(col) {
    const isChecked = this.selectedColumns.find(c => c.name === col.name) != null;
    // console.log('evaluating isChecked for ', col.name, isChecked);createStaticSearchParams
    return isChecked;
  }

  // setLastColumn(array: Array<any>, colName: any) {
  //   let col = array.find(x => x.name === colName);
  //   if (!isNullOrUndefined(col)) {
  //     let posCol = array.indexOf(col);
  //     array.splice(posCol, 1);
  //     array.push(col);
  //   }
  // }

}
