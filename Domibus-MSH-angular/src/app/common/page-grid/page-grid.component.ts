import {AfterViewChecked, ChangeDetectorRef, Component, Input, ViewChild} from '@angular/core';
import {IPageableList, PaginationType} from '../mixins/ipageable-list';
import BaseListComponent from '../mixins/base-list.component';
import {ISortableList} from '../mixins/isortable-list';
import {instanceOfPageableList, instanceOfSortableList} from '../mixins/type.utils';
import {DatatableComponent} from '@swimlane/ngx-datatable';

@Component({
  selector: 'page-grid',
  templateUrl: './page-grid.component.html',
  styleUrls: ['./page-grid.component.css']
})

export class PageGridComponent implements AfterViewChecked {
  @ViewChild('tableWrapper', {static: false}) tableWrapper;
  @ViewChild(DatatableComponent, {static: false}) table: DatatableComponent;
  private currentComponentWidth;

  constructor(private changeDetector: ChangeDetectorRef) {
  }

  @Input()
  parent: BaseListComponent<any> & IPageableList & ISortableList;

  @Input()
  selectionType: undefined | 'single' | 'multi' = undefined;

  @Input()
  sortedColumns: { prop: string, dir: string }[] = [];

  @Input()
  rowClassFn: Function;

  // ugly hack but otherwise the ng-datatable doesn't resize when collapsing the menu
  ngAfterViewChecked() {
    // Check if the table size has changed,
    if (this.table && this.table.recalculate && (this.tableWrapper.nativeElement.clientWidth !== this.currentComponentWidth)) {
      this.currentComponentWidth = this.tableWrapper.nativeElement.clientWidth;
      this.table.recalculate();
      this.changeDetector.detectChanges();

      setTimeout(() => {
        let evt = document.createEvent('HTMLEvents');
        evt.initEvent('resize', true, false);
        window.dispatchEvent(evt)
      }, 100);
    }
  }

  useExternalPaging() {
    return instanceOfPageableList(this.parent) && this.parent.type != PaginationType.Client;
  }

  useExternalSorting() {
    return instanceOfSortableList(this.parent);
  }

}
