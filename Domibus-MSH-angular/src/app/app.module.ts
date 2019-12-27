import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {
  MatButtonModule,
  MatButtonToggleModule, MatCardModule,
  MatCheckboxModule,
  MatDialogModule,
  MatExpansionModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatMenuModule,
  MatSelectModule,
  MatSidenavModule,
  MatTooltipModule
} from '@angular/material';
import 'hammerjs';

import {NgxDatatableModule} from '@swimlane/ngx-datatable';
import {Md2DatepickerModule, MdNativeDateModule} from 'angular-md2';

import {AppComponent} from './app.component';
import {LoginComponent} from './security/login/login.component';
import {CurrentPModeComponent} from './pmode/current/currentPMode.component';
import {PModeArchiveComponent} from './pmode/archive/pmodeArchive.component';

import {AuthenticatedAuthorizedGuard} from './common/guards/authenticated-authorized.guard';
import {routing} from './app.routes';
import {ExtendedHttpInterceptor} from './common/http/extended-http-client';
import {HttpEventService} from './common/http/http.event.service';
import {SecurityService} from './security/security.service';
import {SecurityEventService} from './security/security.event.service';
import {DomainService} from './security/domain.service';
import {AlertComponent} from './common/alert/alert.component';
import {AlertService} from './common/alert/alert.service';
import {ErrorLogComponent} from './errorlog/errorlog.component';
import {FooterComponent} from './common/footer/footer.component';
import {DomibusInfoService} from './common/appinfo/domibusinfo.service';
import {MessageFilterComponent} from './messagefilter/messagefilter.component';
import {MessageLogComponent} from './messagelog/messagelog.component';
import {UserComponent} from './user/user.component';
import {TruststoreComponent} from './truststore/truststore.component';
import {PmodeUploadComponent} from './pmode/pmode-upload/pmode-upload.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {JmsComponent} from './jms/jms.component';
import {RowLimiterComponent} from './common/row-limiter/row-limiter.component';
import {MoveDialogComponent} from './jms/move-dialog/move-dialog.component';
import {MessageDialogComponent} from './jms/message-dialog/message-dialog.component';
import {DatePipe} from './common/customDate/datePipe';
import {CapitalizeFirstPipe} from './common/capitalizefirst.pipe';
import {DefaultPasswordDialogComponent} from './security/default-password-dialog/default-password-dialog.component';
import {MessagelogDetailsComponent} from './messagelog/messagelog-details/messagelog-details.component';
import {ErrorlogDetailsComponent} from './errorlog/errorlog-details/errorlog-details.component';
import {EditMessageFilterComponent} from './messagefilter/editmessagefilter-form/editmessagefilter-form.component';
import {YesNoDialogComponent} from './common/dialogs/yes-no-dialog/yes-no-dialog.component';
import {DirtyGuard} from './common/guards/dirty.guard';
import {EditUserComponent} from 'app/user/edituser-form/edituser-form.component';
import {TruststoreDialogComponent} from './truststore/truststore-dialog/truststore-dialog.component';
import {TrustStoreUploadComponent} from './truststore/truststore-upload/truststore-upload.component';
import {ColumnPickerComponent} from './common/column-picker/column-picker.component';
import {PageHelperComponent} from './common/page-helper/page-helper.component';
import {SharedModule} from './common/module/SharedModule';
import {ActionDirtyDialogComponent} from './pmode/action-dirty-dialog/action-dirty-dialog.component';
import {AuditComponent} from './audit/audit.component';
import {PartyComponent} from './party/party.component';
import {PartyDetailsComponent} from './party/party-details/party-details.component';
import {ClearInvalidDirective} from './common/customDate/clearInvalid.directive';
import {PageHeaderComponent} from './common/page-header/page-header.component';
import {DomainSelectorComponent} from './common/domain-selector/domain-selector.component';
import {PmodeViewComponent} from './pmode/archive/pmode-view/pmode-view.component';
import {AlertsComponent} from './alerts/alerts.component';
import {TestServiceComponent} from './testservice/testservice.component';
import {PluginUserComponent} from './pluginuser/pluginuser.component';
import {EditbasicpluginuserFormComponent} from './pluginuser/editpluginuser-form/editbasicpluginuser-form.component';
import {EditcertificatepluginuserFormComponent} from './pluginuser/editpluginuser-form/editcertificatepluginuser-form.component';
import {PartyIdentifierDetailsComponent} from './party/party-identifier-details/party-identifier-details.component';
import {GlobalErrorHandler} from './common/global.error-handler';
import {UserService} from './user/user.service';
import {UserValidatorService} from './user/uservalidator.service';
import {DefaultPasswordGuard} from './security/defaultPassword.guard';
import {SanitizeHtmlPipe} from './common/sanitizeHtml.pipe';
import {LoggingComponent} from './logging/logging.component';
import {ChangePasswordComponent} from './security/change-password/change-password.component';
import {AuthExternalProviderGuard} from './common/guards/auth-external-provider.guard';
import {LogoutAuthExtProviderComponent} from './security/logout/logout.components';
import {RedirectHomeGuard} from './common/guards/redirect-home.guard';
import {NotAuthorizedComponent} from './security/not-authorized/not-authorized.components';
import {PropertiesComponent} from './properties/properties.component';
import {HttpClientModule} from '@angular/common/http';
import { CommonModule } from '@angular/common';
import {DialogsService} from './common/dialogs/dialogs.service';
import {GridHeaderComponent} from './common/grid-header/grid-header.component';
import {FilterAreaFooterComponent} from './common/filter-area-footer/filter-area-footer.component';
import {PageFooterComponent} from './common/page-footer/page-footer.component';
import {PageGridComponent} from './common/page-grid/page-grid.component';
import {UserCrossFieldValidatorsDirective} from './pluginuser/user-cross-field-validators.directive';


@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    MessageFilterComponent,
    MessageLogComponent,
    UserComponent,
    ErrorLogComponent,
    AlertComponent,
    FooterComponent,
    CurrentPModeComponent,
    PModeArchiveComponent,
    TruststoreComponent,
    PmodeUploadComponent,
    PmodeViewComponent,
    YesNoDialogComponent,
    JmsComponent,
    RowLimiterComponent,
    MoveDialogComponent,
    MessageDialogComponent,
    DatePipe,
    CapitalizeFirstPipe,
    SanitizeHtmlPipe,
    DefaultPasswordDialogComponent,
    EditMessageFilterComponent,
    MessagelogDetailsComponent,
    ErrorlogDetailsComponent,
    EditUserComponent,
    TruststoreDialogComponent,
    TrustStoreUploadComponent,
    ColumnPickerComponent,
    TrustStoreUploadComponent,
    PageHelperComponent,
    ActionDirtyDialogComponent,
    AuditComponent,
    PartyComponent,
    PartyDetailsComponent,
    ClearInvalidDirective,
    PageHeaderComponent,
    DomainSelectorComponent,
    AlertsComponent,
    TestServiceComponent,
    PluginUserComponent,
    EditbasicpluginuserFormComponent,
    EditcertificatepluginuserFormComponent,
    PartyIdentifierDetailsComponent,
    LoggingComponent,
    ChangePasswordComponent,
    LogoutAuthExtProviderComponent,
    NotAuthorizedComponent,
    PropertiesComponent,
    GridHeaderComponent,
    FilterAreaFooterComponent,
    PageFooterComponent,
    PageGridComponent,
    UserCrossFieldValidatorsDirective
  ],
  entryComponents: [
    AppComponent,
    PmodeUploadComponent,
    PmodeViewComponent,
    MoveDialogComponent,
    MessageDialogComponent,
    MessagelogDetailsComponent,
    YesNoDialogComponent,
    DefaultPasswordDialogComponent,
    EditMessageFilterComponent,
    ErrorlogDetailsComponent,
    EditUserComponent,
    TruststoreDialogComponent,
    TrustStoreUploadComponent,
    ActionDirtyDialogComponent,
    PartyDetailsComponent,
    EditbasicpluginuserFormComponent,
    EditcertificatepluginuserFormComponent,
    PartyIdentifierDetailsComponent,
    ChangePasswordComponent,
    PropertiesComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    NgxDatatableModule,
    MatButtonModule,
    MatDialogModule,
    MatTooltipModule,
    MatMenuModule,
    MatInputModule,
    MatIconModule,
    MatListModule,
    MatSidenavModule,
    MatSelectModule,
    routing,
    ReactiveFormsModule,
    Md2DatepickerModule,
    MdNativeDateModule,
    SharedModule,
    MatExpansionModule,
    MatCheckboxModule,
    MatButtonToggleModule,
    MatTooltipModule,
    HttpClientModule,
    MatCardModule,
    CommonModule
  ],
  providers: [
    AuthenticatedAuthorizedGuard,
    DirtyGuard,
    DefaultPasswordGuard,
    AuthExternalProviderGuard,
    RedirectHomeGuard,
    HttpEventService,
    SecurityService,
    SecurityEventService,
    DomainService,
    DomibusInfoService,
    AlertService,
    {provide: HTTP_INTERCEPTORS, useClass: ExtendedHttpInterceptor, multi: true},
    {
      provide: ErrorHandler,
      useClass: GlobalErrorHandler
    },
    UserService,
    UserValidatorService,
    DialogsService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
