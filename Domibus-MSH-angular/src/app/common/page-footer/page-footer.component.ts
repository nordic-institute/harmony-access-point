import {Component, Input, OnInit} from '@angular/core';
import {DomibusInfoService} from '../appinfo/domibusinfo.service';
import BaseListComponent from '../mixins/base-list.component';

@Component({
  templateUrl: 'page-footer.component.html',
  selector: 'page-footer',
  styleUrls: ['./page-footer.component.css']
})

export class PageFooterComponent implements OnInit {

  constructor(private domibusInfoService: DomibusInfoService) {
  }

  @Input()
  parent: BaseListComponent<any>;

  @Input()
  isAddVisible: boolean = true;

  @Input()
  isEditVisible: boolean = true;

  async ngOnInit() {
  }

}
