package dresden.de.blueproject.daggerDependencyInjection;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import dresden.de.blueproject.fragments.DataImportFragment;
import dresden.de.blueproject.fragments.ItemFragment;
import dresden.de.blueproject.fragments.TrayFragment;

/**
 * Diese Klasse bildet das Grundgerüst für die Dagger2 gestützte Abhängigkeitsinjection die alles weitere erleichtert.
 * Entnommen aus https://www.youtube.com/watch?v=LCOKWgHdBvE
 */

@Singleton
@Component(modules = {ApplicationModule.class, RoomModule.class})
public interface ApplicationComponent {

    void inject(TrayFragment trayFragment);
    void inject(DataImportFragment dataImportFragment);
    void inject(ItemFragment itemFragment);

    Application application();

}
