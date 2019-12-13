import {Component, Input} from '@angular/core';
import {IPageableList} from '../mixins/ipageable-list';
import BaseListComponent from '../mixins/base-list.component';
import {ISortableList} from '../mixins/isortable-list';

@Component({
  selector: 'page-grid',
  templateUrl: './page-grid.component.html',
  styleUrls: ['./page-grid.component.css']
})

export class PageGridComponent {

  constructor() {
  }

  @Input()
  parent: BaseListComponent<any> & (IPageableList | ISortableList);

  @Input()
  selectionType: undefined | 'single' | 'multi' = undefined;

  @Input()
  sortedColumns: { prop: string, dir: string }[] = [];

  // get displayedRows(): any[] {
  //   var result = this.rows || this.parent.rows;
  //   return result;
  // }

}
