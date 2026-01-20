-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

CREATE INDEX IF NOT EXISTS idx_inventory_movements_product_id_created_at
  ON inventory_movements(product_id, created_at);
CREATE INDEX IF NOT EXISTS idx_inventory_movements_reference_id
  ON inventory_movements(reference_id);

-- inventory_movements is append-only
CREATE OR REPLACE FUNCTION forbid_inventory_movements_update_delete()
RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'inventory_movements is append-only';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS inventory_movements_no_update ON inventory_movements;
CREATE TRIGGER inventory_movements_no_update
BEFORE UPDATE ON inventory_movements
FOR EACH ROW
EXECUTE FUNCTION forbid_inventory_movements_update_delete();

DROP TRIGGER IF EXISTS inventory_movements_no_delete ON inventory_movements;
CREATE TRIGGER inventory_movements_no_delete
BEFORE DELETE ON inventory_movements
FOR EACH ROW
EXECUTE FUNCTION forbid_inventory_movements_update_delete();
