import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TlDashboard } from './tl-dashboard';

describe('TlDashboard', () => {
  let component: TlDashboard;
  let fixture: ComponentFixture<TlDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TlDashboard],
    }).compileComponents();

    fixture = TestBed.createComponent(TlDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
