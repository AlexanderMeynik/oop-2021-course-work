module main {
    requires com.google.gson;
    requires spring.web;
    exports model to com.google.gson;
    opens model to com.google.gson;
}
