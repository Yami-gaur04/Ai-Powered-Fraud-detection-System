import { useState } from "react";
import { Send } from "lucide-react";

/** The prompt-style bar from the reference design. */
export default function QuickBar({ placeholder, button, onSubmit }) {
  const [v, setV] = useState("");
  const go = () => onSubmit(v, () => setV(""));
  return (
    <div className="cmd">
      <Send size={22} />
      <input value={v} onChange={(e) => setV(e.target.value)} placeholder={placeholder} onKeyDown={(e) => e.key === "Enter" && go()} />
      <button className="btn gray" onClick={go}>{button}</button>
    </div>
  );
}
