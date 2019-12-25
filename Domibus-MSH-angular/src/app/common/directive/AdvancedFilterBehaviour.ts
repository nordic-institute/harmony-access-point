import {Directive, ElementRef, Host, Input, OnInit, Optional} from '@angular/core';
import {NgControl} from '@angular/forms';
import BaseListComponent from '../mixins/base-list.component';
import {IFilterableList} from '../mixins/ifilterable-list';

@Directive({
  selector: '[advanced-filter-behaviour]'
})
export class AdvancedFilterBehaviourDirective implements OnInit {
  constructor(private control: NgControl) {
  }

  @Input()
  parent: IFilterableList;

  ngOnInit() {
    this.parent.advancedFilters.add(this.control.name);
  }

}
