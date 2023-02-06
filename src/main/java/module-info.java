module ru.antebeotcli.antebeotcli_ {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.eclipse.jetty.client;
    requires org.kordamp.bootstrapfx.core;
    opens ru.antebeotcli.antebeotcli_ to javafx.fxml;
    exports ru.antebeotcli.antebeotcli_;
}