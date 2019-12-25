import {NgModule} from "@angular/core";

import {ClickStopPropagationDirective} from 'app/common/directive/attribute/ClickStopPropagation';
import {ButtonClickBehaviourDirective} from '../directive/ButtonClickBehaviour';
import {AdvancedFilterBehaviourDirective} from '../directive/AdvancedFilterBehaviour';

@NgModule({
  declarations: [
    ClickStopPropagationDirective,
    ButtonClickBehaviourDirective,
    AdvancedFilterBehaviourDirective
  ],
  exports: [
    ClickStopPropagationDirective,
    ButtonClickBehaviourDirective,
    AdvancedFilterBehaviourDirective
  ]
})
export class SharedModule {
}
