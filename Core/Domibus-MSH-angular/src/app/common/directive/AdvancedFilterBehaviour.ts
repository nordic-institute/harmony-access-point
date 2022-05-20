import {Directive, Input, OnInit} from '@angular/core';
import {NgControl} from '@angular/forms';
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
