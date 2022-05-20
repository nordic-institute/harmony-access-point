import {Component, Input, SimpleChanges} from '@angular/core';
import {EditPopupBaseComponent} from '../edit-popup-base.component';

@Component({
  templateUrl: 'edit-popup-footer.component.html',
  selector: 'popup-edit-footer',
  styleUrls: ['./edit-popup-footer.component.css']
})

export class EditPopupFooterComponent {

  constructor() {
  }

  @Input()
  parent: EditPopupBaseComponent;

}
