package io.github.ollama4j.webui.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelItem {
  private String name;
  private String version;
}
